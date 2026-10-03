package com.chinesereads.academy.classroom.internal;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InviteCodeRepository extends JpaRepository<InviteCode, Long> {

  Optional<InviteCode> findByCode(String code);

  boolean existsByCode(String code);

  @Query("select i from InviteCode i where i.group.id = :groupId and i.revokedAt is null and i.expiresAt > :now order by i.createdAt desc limit 1")
  Optional<InviteCode> findActiveByGroupId(@Param("groupId") long groupId, @Param("now") Instant now);
}
