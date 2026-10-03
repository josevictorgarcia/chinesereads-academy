package com.chinesereads.academy.identity.web;

import com.chinesereads.academy.identity.AcademyRole;
import com.chinesereads.academy.identity.CurrentUser;
import com.chinesereads.academy.identity.CurrentUserService;
import com.chinesereads.academy.identity.TeacherProfileService;
import com.chinesereads.academy.identity.TeacherProfileView;
import java.util.Set;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class MeController {

  private final CurrentUserService currentUser;
  private final TeacherProfileService teachers;

  public MeController(CurrentUserService currentUser, TeacherProfileService teachers) {
    this.currentUser = currentUser;
    this.teachers = teachers;
  }

  @GetMapping
  public MeResponse me() {
    CurrentUser user = currentUser.require();
    TeacherSummary teacher = teachers.findByUserId(user.userId())
        .map(t -> new TeacherSummary(t.id(), t.displayName()))
        .orElse(null);
    return new MeResponse(user.userId(), user.email(), user.name(), user.language(), user.roles(), teacher);
  }

  public record MeResponse(long userId, String email, String name, String language, Set<AcademyRole> roles,
      TeacherSummary teacher) {}

  public record TeacherSummary(long id, String displayName) {}

  static TeacherSummary summary(TeacherProfileView view) {
    return new TeacherSummary(view.id(), view.displayName());
  }
}
