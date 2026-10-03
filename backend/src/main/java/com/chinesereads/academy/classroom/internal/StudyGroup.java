package com.chinesereads.academy.classroom.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "study_group")
public class StudyGroup {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "teacher_id", nullable = false)
  private long teacherId;

  @Column(name = "name", nullable = false, length = 80)
  private String name;

  @Column(name = "level", length = 8)
  private String level;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  protected StudyGroup() {}

  public StudyGroup(long teacherId, String name, String level, String description, Instant now) {
    this.teacherId = teacherId;
    this.name = name;
    this.level = level;
    this.description = description;
    this.createdAt = now;
    this.updatedAt = now;
  }

  public Long getId() { return id; }
  public long getTeacherId() { return teacherId; }
  public String getName() { return name; }
  public String getLevel() { return level; }
  public String getDescription() { return description; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public Instant getDeletedAt() { return deletedAt; }
}
