package com.chinesereads.academy.classroom.internal;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

  Optional<Enrollment> findByTeacherIdAndStudentUserId(long teacherId, long studentUserId);

  Optional<Enrollment> findByIdAndTeacherId(long id, long teacherId);

  @Query("select count(e) from Enrollment e where e.teacherId = :teacherId and e.status = com.chinesereads.academy.classroom.internal.Enrollment.Status.ACTIVE")
  int countActiveByTeacherId(@Param("teacherId") long teacherId);

  @Query("select count(e) > 0 from Enrollment e where e.studentUserId = :studentUserId and e.status = com.chinesereads.academy.classroom.internal.Enrollment.Status.ACTIVE")
  boolean hasActiveSeat(@Param("studentUserId") long studentUserId);
}
