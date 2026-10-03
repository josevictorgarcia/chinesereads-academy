package com.chinesereads.academy.classroom.web;

import com.chinesereads.academy.classroom.ClassroomViews.JoinResult;
import com.chinesereads.academy.classroom.ClassroomViews.StudentGroup;
import com.chinesereads.academy.classroom.EnrollmentService;
import com.chinesereads.academy.identity.CurrentUserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoints del alumno (cualquier usuario autenticado de ChineseReads puede unirse con un código). */
@RestController
@RequestMapping("/api/student")
public class StudentController {

  private final CurrentUserService currentUser;
  private final EnrollmentService enrollments;

  public StudentController(CurrentUserService currentUser, EnrollmentService enrollments) {
    this.currentUser = currentUser;
    this.enrollments = enrollments;
  }

  @PostMapping("/join")
  public JoinResult join(@Valid @RequestBody JoinRequest request) {
    return enrollments.join(currentUser.require(), request.code());
  }

  @GetMapping("/groups")
  public List<StudentGroup> groups() {
    return enrollments.groupsOf(currentUser.require().userId());
  }

  public record JoinRequest(@NotBlank @Size(min = 8, max = 8) String code) {}
}
