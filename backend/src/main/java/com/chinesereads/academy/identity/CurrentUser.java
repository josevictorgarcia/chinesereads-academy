package com.chinesereads.academy.identity;

import java.util.Set;

/** Usuario autenticado en la petición actual (datos de chinesereads.user + roles de Academy). */
public record CurrentUser(long userId, String email, String name, String language, Set<AcademyRole> roles) {

  public boolean has(AcademyRole role) {
    return roles.contains(role);
  }
}
