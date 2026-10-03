package com.chinesereads.academy.access.internal;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PremiumGrantRepository extends JpaRepository<PremiumGrant, Long> {

  List<PremiumGrant> findByStatusInAndAttemptsLessThanOrderByUpdatedAtAsc(List<PremiumGrant.Status> statuses, int maxAttempts);

  List<PremiumGrant> findByEnrollmentIdOrderByCreatedAtDesc(long enrollmentId);
}
