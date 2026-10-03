package com.chinesereads.academy.identity;

/** Roles propios de Academy. No se leen del token: se derivan de los datos de Academy. */
public enum AcademyRole {
  TEACHER,
  STUDENT;

  public String authority() {
    return "ROLE_" + name();
  }
}
