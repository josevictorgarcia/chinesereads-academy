package com.chinesereads.academy.classroom.web;

import com.chinesereads.academy.classroom.ClassroomViews.GroupDetail;
import com.chinesereads.academy.classroom.ClassroomViews.GroupSummary;
import com.chinesereads.academy.classroom.ClassroomViews.InviteView;
import com.chinesereads.academy.classroom.ClassroomViews.SeatUsage;
import com.chinesereads.academy.classroom.GroupService;
import com.chinesereads.academy.identity.CurrentUserService;
import com.chinesereads.academy.identity.TeacherProfileService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Endpoints del profesor. Todos exigen rol TEACHER (el perfil se resuelve del usuario actual). */
@RestController
@RequestMapping("/api/teacher")
public class TeacherGroupsController {

  private final CurrentUserService currentUser;
  private final TeacherProfileService teachers;
  private final GroupService groups;

  public TeacherGroupsController(CurrentUserService currentUser, TeacherProfileService teachers, GroupService groups) {
    this.currentUser = currentUser;
    this.teachers = teachers;
    this.groups = groups;
  }

  @GetMapping("/groups")
  public List<GroupSummary> list() {
    return groups.list(teacherId());
  }

  @PostMapping("/groups")
  @ResponseStatus(HttpStatus.CREATED)
  public GroupSummary create(@Valid @RequestBody CreateGroupRequest request) {
    return groups.create(teacherId(), request.name(), request.level(), request.description());
  }

  @GetMapping("/groups/{groupId}")
  public GroupDetail detail(@PathVariable long groupId) {
    return groups.detail(teacherId(), groupId);
  }

  @PostMapping("/groups/{groupId}/invite")
  @ResponseStatus(HttpStatus.CREATED)
  public InviteView invite(@PathVariable long groupId) {
    return groups.createInvite(teacherId(), groupId);
  }

  @DeleteMapping("/groups/{groupId}/invite")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void revokeInvite(@PathVariable long groupId) {
    groups.revokeInvite(teacherId(), groupId);
  }

  @DeleteMapping("/enrollments/{enrollmentId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deactivate(@PathVariable long enrollmentId) {
    groups.deactivateEnrollment(teacherId(), enrollmentId);
  }

  @GetMapping("/seats")
  public SeatUsage seats() {
    return groups.seatUsage(teacherId());
  }

  private long teacherId() {
    var user = currentUser.requireTeacher();
    return teachers.findByUserId(user.userId()).orElseThrow().id();
  }

  public record CreateGroupRequest(@NotBlank @Size(max = 80) String name, @Size(max = 8) String level,
      @Size(max = 2000) String description) {}
}
