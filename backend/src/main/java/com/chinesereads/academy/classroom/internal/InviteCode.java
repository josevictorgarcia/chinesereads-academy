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
@Table(name = "invite_code")
public class InviteCode {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "group_id", nullable = false)
  private StudyGroup group;

  @Column(name = "code", nullable = false, unique = true, length = 8)
  private String code;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "max_uses")
  private Integer maxUses;

  @Column(name = "uses", nullable = false)
  private int uses;

  @Column(name = "revoked_at")
  private Instant revokedAt;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected InviteCode() {}

  public InviteCode(StudyGroup group, String code, Instant now, Instant expiresAt, Integer maxUses) {
    this.group = group;
    this.code = code;
    this.createdAt = now;
    this.expiresAt = expiresAt;
    this.maxUses = maxUses;
  }

  public boolean isUsableAt(Instant now) {
    return revokedAt == null && expiresAt.isAfter(now) && (maxUses == null || uses < maxUses);
  }

  public void registerUse() {
    this.uses++;
  }

  public void revoke(Instant now) {
    this.revokedAt = now;
  }

  public Long getId() { return id; }
  public StudyGroup getGroup() { return group; }
  public String getCode() { return code; }
  public Instant getExpiresAt() { return expiresAt; }
  public Integer getMaxUses() { return maxUses; }
  public int getUses() { return uses; }
  public Instant getRevokedAt() { return revokedAt; }
}
