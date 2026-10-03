package com.chinesereads.academy.classroom.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "group_member")
public class GroupMember {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "group_id", nullable = false)
  private StudyGroup group;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "enrollment_id", nullable = false)
  private Enrollment enrollment;

  @Column(name = "joined_at", nullable = false)
  private Instant joinedAt;

  @Column(name = "left_at")
  private Instant leftAt;

  protected GroupMember() {}

  public GroupMember(StudyGroup group, Enrollment enrollment, Instant now) {
    this.group = group;
    this.enrollment = enrollment;
    this.joinedAt = now;
  }

  public void leave(Instant now) {
    this.leftAt = now;
  }

  public Long getId() { return id; }
  public StudyGroup getGroup() { return group; }
  public Enrollment getEnrollment() { return enrollment; }
  public Instant getJoinedAt() { return joinedAt; }
  public Instant getLeftAt() { return leftAt; }
}
