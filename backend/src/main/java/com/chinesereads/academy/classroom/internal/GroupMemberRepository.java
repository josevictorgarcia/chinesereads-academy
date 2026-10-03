package com.chinesereads.academy.classroom.internal;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

  @Query("select m from GroupMember m join fetch m.enrollment where m.group.id = :groupId and m.leftAt is null order by m.joinedAt")
  List<GroupMember> findActiveByGroupId(@Param("groupId") long groupId);

  @Query("select count(m) from GroupMember m where m.group.id = :groupId and m.leftAt is null")
  int countActiveByGroupId(@Param("groupId") long groupId);

  @Query("select m from GroupMember m where m.group.id = :groupId and m.enrollment.id = :enrollmentId and m.leftAt is null")
  Optional<GroupMember> findActiveByGroupIdAndEnrollmentId(@Param("groupId") long groupId, @Param("enrollmentId") long enrollmentId);

  @Query("select m from GroupMember m where m.enrollment.id = :enrollmentId and m.leftAt is null")
  List<GroupMember> findActiveByEnrollmentId(@Param("enrollmentId") long enrollmentId);

  @Query("select m from GroupMember m join fetch m.group g where m.enrollment.studentUserId = :studentUserId and m.leftAt is null and g.deletedAt is null order by m.joinedAt desc")
  List<GroupMember> findActiveByStudentUserId(@Param("studentUserId") long studentUserId);
}
