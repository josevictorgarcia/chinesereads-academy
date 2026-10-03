package com.chinesereads.academy.identity.web;

import com.chinesereads.academy.identity.CurrentUserService;
import com.chinesereads.academy.identity.TeacherProfileService;
import com.chinesereads.academy.identity.TeacherProfileView;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

  private final CurrentUserService currentUser;
  private final TeacherProfileService teachers;

  public TeacherController(CurrentUserService currentUser, TeacherProfileService teachers) {
    this.currentUser = currentUser;
    this.teachers = teachers;
  }

  /** El usuario autenticado se da de alta como profesor. */
  @PostMapping("/me")
  @ResponseStatus(HttpStatus.CREATED)
  public TeacherResponse register(@Valid @RequestBody RegisterTeacherRequest request) {
    TeacherProfileView view = teachers.register(currentUser.require(), request.displayName());
    return TeacherResponse.of(view);
  }

  @GetMapping("/me")
  public TeacherResponse me() {
    var user = currentUser.requireTeacher();
    return teachers.findByUserId(user.userId()).map(TeacherResponse::of)
        .orElseThrow(() -> new IllegalStateException("TEACHER role without profile"));
  }

  public record RegisterTeacherRequest(@NotBlank @Size(max = 120) String displayName) {}

  public record TeacherResponse(long id, String displayName, String language) {
    static TeacherResponse of(TeacherProfileView v) {
      return new TeacherResponse(v.id(), v.displayName(), v.language());
    }
  }
}
