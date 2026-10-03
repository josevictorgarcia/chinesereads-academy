package com.chinesereads.academy.access.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Auditoría y cola de reintentos de cada concesión/retirada de premium. */
@Entity
@Table(name = "premium_grant")
public class PremiumGrant {

  public enum Action { GRANT, REVOKE }

  public enum Status { PENDING, DONE, FAILED }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "enrollment_id", nullable = false)
  private long enrollmentId;

  @Column(name = "student_user_id", nullable = false)
  private long studentUserId;

  @Enumerated(EnumType.STRING)
  @Column(name = "action", nullable = false, length = 8)
  private Action action;

  @Column(name = "granted_until")
  private Instant grantedUntil;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 16)
  private Status status;

  @Column(name = "attempts", nullable = false)
  private int attempts;

  @Column(name = "last_error", length = 500)
  private String lastError;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected PremiumGrant() {}

  public PremiumGrant(long enrollmentId, long studentUserId, Action action, Instant grantedUntil, Instant now) {
    this.enrollmentId = enrollmentId;
    this.studentUserId = studentUserId;
    this.action = action;
    this.grantedUntil = grantedUntil;
    this.status = Status.PENDING;
    this.createdAt = now;
    this.updatedAt = now;
  }

  public void markDone(Instant now) {
    this.status = Status.DONE;
    this.attempts++;
    this.lastError = null;
    this.updatedAt = now;
  }

  public void markFailed(String error, Instant now) {
    this.status = Status.FAILED;
    this.attempts++;
    this.lastError = error == null ? null : error.substring(0, Math.min(500, error.length()));
    this.updatedAt = now;
  }

  public String reference() {
    return "enrollment:" + enrollmentId;
  }

  public Long getId() { return id; }
  public long getEnrollmentId() { return enrollmentId; }
  public long getStudentUserId() { return studentUserId; }
  public Action getAction() { return action; }
  public Instant getGrantedUntil() { return grantedUntil; }
  public Status getStatus() { return status; }
  public int getAttempts() { return attempts; }
  public String getLastError() { return lastError; }
}
