package com.chinesereads.academy.classroom;

import com.chinesereads.academy.billing.SeatAllowance;
import com.chinesereads.academy.billing.SeatQuotaService;
import com.chinesereads.academy.classroom.ClassroomViews.GroupDetail;
import com.chinesereads.academy.classroom.ClassroomViews.GroupSummary;
import com.chinesereads.academy.classroom.ClassroomViews.InviteView;
import com.chinesereads.academy.classroom.ClassroomViews.Member;
import com.chinesereads.academy.classroom.ClassroomViews.SeatUsage;
import com.chinesereads.academy.classroom.internal.Enrollment;
import com.chinesereads.academy.classroom.internal.EnrollmentRepository;
import com.chinesereads.academy.classroom.internal.GroupMemberRepository;
import com.chinesereads.academy.classroom.internal.InviteCode;
import com.chinesereads.academy.classroom.internal.InviteCodeRepository;
import com.chinesereads.academy.classroom.internal.StudentDirectory;
import com.chinesereads.academy.classroom.internal.StudyGroup;
import com.chinesereads.academy.classroom.internal.StudyGroupRepository;
import com.chinesereads.academy.shared.error.AcademyException;
import com.chinesereads.academy.shared.error.ErrorCode;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Operaciones del profesor sobre sus grupos, invitaciones y asientos. */
@Service
public class GroupService {

  private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // sin 0/O/1/I
  private static final SecureRandom RANDOM = new SecureRandom();

  private final StudyGroupRepository groups;
  private final EnrollmentRepository enrollments;
  private final GroupMemberRepository members;
  private final InviteCodeRepository invites;
  private final StudentDirectory students;
  private final SeatQuotaService quota;
  private final SeatEvents events;
  private final Clock clock;
  private final Duration inviteTtl;

  public GroupService(StudyGroupRepository groups, EnrollmentRepository enrollments, GroupMemberRepository members,
      InviteCodeRepository invites, StudentDirectory students, SeatQuotaService quota, SeatEvents events, Clock clock,
      @Value("${academy.classroom.invite-ttl:P30D}") Duration inviteTtl) {
    this.groups = groups;
    this.enrollments = enrollments;
    this.members = members;
    this.invites = invites;
    this.students = students;
    this.quota = quota;
    this.events = events;
    this.clock = clock;
    this.inviteTtl = inviteTtl;
  }

  @Transactional
  public GroupSummary create(long teacherId, String name, String level, String description) {
    Instant now = Instant.now(clock);
    StudyGroup group = groups.save(new StudyGroup(teacherId, name.trim(), blankToNull(level), blankToNull(description), now));
    return new GroupSummary(group.getId(), group.getName(), group.getLevel(), 0, group.getCreatedAt());
  }

  @Transactional(readOnly = true)
  public List<GroupSummary> list(long teacherId) {
    return groups.findByTeacherIdAndDeletedAtIsNullOrderByCreatedAtDesc(teacherId).stream()
        .map(g -> new GroupSummary(g.getId(), g.getName(), g.getLevel(), members.countActiveByGroupId(g.getId()),
            g.getCreatedAt()))
        .toList();
  }

  @Transactional(readOnly = true)
  public GroupDetail detail(long teacherId, long groupId) {
    StudyGroup group = owned(teacherId, groupId);
    Instant now = Instant.now(clock);
    List<Member> memberViews = members.findActiveByGroupId(groupId).stream()
        .map(m -> {
          Enrollment e = m.getEnrollment();
          var who = students.lookup(e.getStudentUserId());
          return new Member(e.getId(), e.getStudentUserId(), who.map(StudentDirectory.Student::name).orElse("—"),
              who.map(StudentDirectory.Student::email).orElse(null), m.getJoinedAt());
        })
        .toList();
    InviteView invite = invites.findActiveByGroupId(groupId, now).map(GroupService::toView).orElse(null);
    return new GroupDetail(group.getId(), group.getName(), group.getLevel(), group.getDescription(), memberViews,
        invite, seatUsage(teacherId));
  }

  @Transactional
  public InviteView createInvite(long teacherId, long groupId) {
    StudyGroup group = owned(teacherId, groupId);
    Instant now = Instant.now(clock);
    invites.findActiveByGroupId(groupId, now).ifPresent(i -> i.revoke(now));
    String code;
    do {
      code = randomCode();
    } while (invites.existsByCode(code));
    InviteCode invite = invites.save(new InviteCode(group, code, now, now.plus(inviteTtl), null));
    return toView(invite);
  }

  @Transactional
  public void revokeInvite(long teacherId, long groupId) {
    owned(teacherId, groupId);
    invites.findActiveByGroupId(groupId, Instant.now(clock)).ifPresent(i -> i.revoke(Instant.now(clock)));
  }

  /** Baja de un alumno (su asiento) con este profesor: sale de todos los grupos y se retira el premium. */
  @Transactional
  public void deactivateEnrollment(long teacherId, long enrollmentId) {
    Enrollment enrollment = enrollments.findByIdAndTeacherId(enrollmentId, teacherId)
        .orElseThrow(() -> new AcademyException(ErrorCode.CLASSROOM_ENROLLMENT_NOT_FOUND, "Enrollment not found"));
    if (!enrollment.isActive()) {
      return;
    }
    Instant now = Instant.now(clock);
    enrollment.deactivate(now);
    members.findActiveByEnrollmentId(enrollmentId).forEach(m -> m.leave(now));
    events.publish(new SeatDeactivated(enrollment.getId(), teacherId, enrollment.getStudentUserId()));
  }

  @Transactional(readOnly = true)
  public SeatUsage seatUsage(long teacherId) {
    Optional<SeatAllowance> allowance = quota.allowanceFor(teacherId);
    int used = enrollments.countActiveByTeacherId(teacherId);
    return new SeatUsage(used, allowance.map(SeatAllowance::totalSeats).orElse(0),
        allowance.map(SeatAllowance::periodEnd).orElse(null), allowance.map(SeatAllowance::active).orElse(false));
  }

  private StudyGroup owned(long teacherId, long groupId) {
    return groups.findByIdAndTeacherIdAndDeletedAtIsNull(groupId, teacherId)
        .orElseThrow(() -> new AcademyException(ErrorCode.CLASSROOM_GROUP_NOT_FOUND, "Group not found"));
  }

  private static InviteView toView(InviteCode i) {
    return new InviteView(i.getCode(), i.getExpiresAt(), i.getMaxUses(), i.getUses());
  }

  private static String randomCode() {
    StringBuilder sb = new StringBuilder(8);
    for (int i = 0; i < 8; i++) {
      sb.append(CODE_ALPHABET.charAt(RANDOM.nextInt(CODE_ALPHABET.length())));
    }
    return sb.toString();
  }

  private static String blankToNull(String s) {
    return s == null || s.isBlank() ? null : s.trim();
  }

}
