package com.chinesereads.academy.classroom.internal;

import com.chinesereads.academy.identity.TeacherProfileService;
import com.chinesereads.academy.identity.UserDirectory;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Adaptador fino a identity: nombres de alumnos y profesores para las vistas. */
@Component
public class StudentDirectory {

  private final UserDirectory users;
  private final TeacherProfileService teachers;

  public StudentDirectory(UserDirectory users, TeacherProfileService teachers) {
    this.users = users;
    this.teachers = teachers;
  }

  public Optional<Student> lookup(long userId) {
    return users.byId(userId).map(u -> new Student(u.id(), u.name(), u.email()));
  }

  public Optional<String> teacherDisplayName(long teacherId) {
    return teachers.findById(teacherId).map(t -> t.displayName());
  }

  /** ¿Es este usuario el dueño del perfil de profesor indicado? */
  public boolean isTeacherUser(long teacherId, long userId) {
    return teachers.findById(teacherId).map(t -> t.userId() == userId).orElse(false);
  }

  public record Student(long id, String name, String email) {}
}
