package com.chinesereads.academy.classroom.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Asiento: un alumno (chinesereads.user.id) dado de alta con un profesor. */
@Entity
@Table(name = "enrollment")
public class Enrollment {

  public enum Status { ACTIVE, DEACTIVATED }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "teacher_id", nullable = false)
  private long teacherId;

  @Column(name = "student_user_id", nullable = false)
  private long studentUserId;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 16)
  private Status status;

  @Column(name = "activated_at", nullable = false)
  private Instant activatedAt;

  @Column(name = "deactivated_at")
  private Instant deactivatedAt;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected Enrollment() {}

  public Enrollment(long teacherId, long studentUserId, Instant now) {
    this.teacherId = teacherId;
    this.studentUserId = studentUserId;
    this.status = Status.ACTIVE;
    this.activatedAt = now;
    this.createdAt = now;
    this.updatedAt = now;
  }

  public boolean isActive() {
    return status == Status.ACTIVE;
  }

  public void deactivate(Instant now) {
    this.status = Status.DEACTIVATED;
    this.deactivatedAt = now;
    this.updatedAt = now;
  }

  public void reactivate(Instant now) {
    this.status = Status.ACTIVE;
    this.activatedAt = now;
    this.deactivatedAt = null;
    this.updatedAt = now;
  }

  public Long getId() { return id; }
  public long getTeacherId() { return teacherId; }
  public long getStudentUserId() { return studentUserId; }
  public Status getStatus() { return status; }
  public Instant getActivatedAt() { return activatedAt; }
  public Instant getDeactivatedAt() { return deactivatedAt; }
}
