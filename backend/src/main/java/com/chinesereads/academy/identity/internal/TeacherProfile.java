package com.chinesereads.academy.identity.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "teacher_profile")
public class TeacherProfile {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** Referencia a chinesereads.user.id (sin FK física: esquema ajeno). */
  @Column(name = "user_id", nullable = false, unique = true)
  private long userId;

  @Column(name = "display_name", nullable = false, length = 120)
  private String displayName;

  @Column(name = "language", nullable = false, length = 2)
  private String language;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  protected TeacherProfile() {}

  public TeacherProfile(long userId, String displayName, String language, Instant now) {
    this.userId = userId;
    this.displayName = displayName;
    this.language = language;
    this.createdAt = now;
    this.updatedAt = now;
  }

  public Long getId() {
    return id;
  }

  public long getUserId() {
    return userId;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getLanguage() {
    return language;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public Instant getDeletedAt() {
    return deletedAt;
  }

  public void rename(String displayName, Instant now) {
    this.displayName = displayName;
    this.updatedAt = now;
  }
}
