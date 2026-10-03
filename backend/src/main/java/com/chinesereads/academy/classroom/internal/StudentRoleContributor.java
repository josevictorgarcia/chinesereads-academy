package com.chinesereads.academy.classroom.internal;

import com.chinesereads.academy.identity.AcademyRole;
import com.chinesereads.academy.identity.RoleContributor;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Un usuario con algún asiento activo tiene el rol STUDENT. */
@Component
public class StudentRoleContributor implements RoleContributor {

  private final EnrollmentRepository enrollments;

  public StudentRoleContributor(EnrollmentRepository enrollments) {
    this.enrollments = enrollments;
  }

  @Override
  @Transactional(readOnly = true)
  public Set<AcademyRole> rolesFor(long userId) {
    return enrollments.hasActiveSeat(userId) ? Set.of(AcademyRole.STUDENT) : Set.of();
  }
}
