package com.chinesereads.academy.identity.internal;

import com.chinesereads.academy.identity.AcademyRole;
import com.chinesereads.academy.identity.RoleContributor;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Un usuario con perfil de profesor activo tiene el rol TEACHER. */
@Component
public class TeacherRoleContributor implements RoleContributor {

  private final TeacherProfileRepository repository;

  public TeacherRoleContributor(TeacherProfileRepository repository) {
    this.repository = repository;
  }

  @Override
  @Transactional(readOnly = true)
  public Set<AcademyRole> rolesFor(long userId) {
    return repository.existsByUserIdAndDeletedAtIsNull(userId) ? Set.of(AcademyRole.TEACHER) : Set.of();
  }
}
