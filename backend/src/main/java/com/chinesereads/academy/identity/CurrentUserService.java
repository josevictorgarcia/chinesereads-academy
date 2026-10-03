package com.chinesereads.academy.identity;

import com.chinesereads.academy.shared.error.AcademyException;
import com.chinesereads.academy.shared.error.ErrorCode;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/** Acceso tipado al usuario de la petición actual. */
@Service
public class CurrentUserService {

  public Optional<CurrentUser> current() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CurrentUser user) {
      return Optional.of(user);
    }
    return Optional.empty();
  }

  public CurrentUser require() {
    return current().orElseThrow(() -> new AcademyException(ErrorCode.UNAUTHENTICATED, "Authentication required"));
  }

  public CurrentUser requireTeacher() {
    CurrentUser user = require();
    if (!user.has(AcademyRole.TEACHER)) {
      throw new AcademyException(ErrorCode.IDENTITY_TEACHER_REQUIRED, "Teacher profile required");
    }
    return user;
  }
}
