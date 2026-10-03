package com.chinesereads.academy.classroom;

import com.chinesereads.academy.billing.SeatAllowance;
import com.chinesereads.academy.billing.SeatQuotaService;
import com.chinesereads.academy.classroom.ClassroomViews.JoinResult;
import com.chinesereads.academy.classroom.ClassroomViews.StudentGroup;
import com.chinesereads.academy.classroom.internal.Enrollment;
import com.chinesereads.academy.classroom.internal.EnrollmentRepository;
import com.chinesereads.academy.classroom.internal.GroupMember;
import com.chinesereads.academy.classroom.internal.GroupMemberRepository;
import com.chinesereads.academy.classroom.internal.InviteCode;
import com.chinesereads.academy.classroom.internal.InviteCodeRepository;
import com.chinesereads.academy.classroom.internal.StudentDirectory;
import com.chinesereads.academy.classroom.internal.StudyGroup;
import com.chinesereads.academy.identity.CurrentUser;
import com.chinesereads.academy.shared.error.AcademyException;
import com.chinesereads.academy.shared.error.ErrorCode;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lado del alumno: unirse a un grupo con un código y ver sus grupos. */
@Service
public class EnrollmentService {

  private final InviteCodeRepository invites;
  private final EnrollmentRepository enrollments;
  private final GroupMemberRepository members;
  private final StudentDirectory directory;
  private final SeatQuotaService quota;
  private final SeatEvents events;
  private final Clock clock;

  public EnrollmentService(InviteCodeRepository invites, EnrollmentRepository enrollments, GroupMemberRepository members,
      StudentDirectory directory, SeatQuotaService quota, SeatEvents events, Clock clock) {
    this.invites = invites;
    this.enrollments = enrollments;
    this.members = members;
    this.directory = directory;
    this.quota = quota;
    this.events = events;
    this.clock = clock;
  }

  /**
   * Reglas: código válido y vigente; el alumno no puede ser el propio profesor; un alumno ocupa UN asiento por
   * profesor aunque esté en varios grupos; si ya tiene asiento activo con ese profesor, solo se añade al grupo
   * (sin consumir cuota ni volver a conceder premium); si no, se comprueba la cuota y se publica SeatActivated.
   */
  @Transactional
  public JoinResult join(CurrentUser student, String rawCode) {
    Instant now = Instant.now(clock);
    String code = rawCode == null ? "" : rawCode.trim().toUpperCase();
    InviteCode invite = invites.findByCode(code)
        .filter(i -> i.isUsableAt(now))
        .orElseThrow(() -> new AcademyException(ErrorCode.CLASSROOM_INVITE_INVALID, "Invite code invalid or expired"));
    StudyGroup group = invite.getGroup();
    long teacherId = group.getTeacherId();
    if (directory.isTeacherUser(teacherId, student.userId())) {
      throw new AcademyException(ErrorCode.CLASSROOM_CANNOT_JOIN_OWN_GROUP, "A teacher cannot join their own group");
    }

    Enrollment enrollment = enrollments.findByTeacherIdAndStudentUserId(teacherId, student.userId()).orElse(null);
    boolean newSeat = enrollment == null || !enrollment.isActive();
    SeatAllowance allowance = quota.allowanceFor(teacherId)
        .filter(SeatAllowance::active)
        .orElseThrow(() -> new AcademyException(ErrorCode.BILLING_SUBSCRIPTION_INACTIVE, "Teacher subscription is not active"));

    if (newSeat) {
      int used = enrollments.countActiveByTeacherId(teacherId);
      if (used >= allowance.totalSeats()) {
        throw new AcademyException(ErrorCode.BILLING_SEAT_QUOTA_EXCEEDED,
            "Seat quota exceeded (" + used + "/" + allowance.totalSeats() + ")");
      }
      if (enrollment == null) {
        enrollment = enrollments.save(new Enrollment(teacherId, student.userId(), now));
      } else {
        enrollment.reactivate(now);
      }
    }

    boolean alreadyMember = members.findActiveByGroupIdAndEnrollmentId(group.getId(), enrollment.getId()).isPresent();
    if (!alreadyMember) {
      members.save(new GroupMember(group, enrollment, now));
      invite.registerUse();
    }
    if (newSeat) {
      events.publish(new SeatActivated(enrollment.getId(), teacherId, student.userId(), allowance.periodEnd()));
    }
    return new JoinResult(group.getId(), group.getName(), alreadyMember);
  }

  @Transactional(readOnly = true)
  public List<StudentGroup> groupsOf(long studentUserId) {
    return members.findActiveByStudentUserId(studentUserId).stream()
        .map(m -> new StudentGroup(m.getGroup().getId(), m.getGroup().getName(), m.getGroup().getLevel(),
            directory.teacherDisplayName(m.getGroup().getTeacherId()).orElse("—"), m.getJoinedAt()))
        .toList();
  }
}
