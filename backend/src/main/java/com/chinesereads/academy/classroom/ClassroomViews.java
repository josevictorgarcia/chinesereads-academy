package com.chinesereads.academy.classroom;

import java.time.Instant;
import java.util.List;

/** DTOs públicos del módulo (espejo en frontend/src/app/api/models.ts). */
public final class ClassroomViews {

  private ClassroomViews() {}

  public record GroupSummary(long id, String name, String level, int activeMembers, Instant createdAt) {}

  public record GroupDetail(long id, String name, String level, String description, List<Member> members,
      InviteView activeInvite, SeatUsage seats) {}

  public record Member(long enrollmentId, long studentUserId, String name, String email, Instant joinedAt) {}

  public record InviteView(String code, Instant expiresAt, Integer maxUses, int uses) {}

  public record SeatUsage(int used, int total, Instant periodEnd, boolean subscriptionActive) {}

  public record StudentGroup(long groupId, String name, String level, String teacherName, Instant joinedAt) {}

  public record JoinResult(long groupId, String groupName, boolean alreadyMember) {}
}
