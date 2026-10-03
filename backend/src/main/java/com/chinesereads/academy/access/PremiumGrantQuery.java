package com.chinesereads.academy.access;

import com.chinesereads.academy.access.internal.PremiumGrantRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consulta de auditoría (panel del profesor / tests). */
@Service
public class PremiumGrantQuery {

  private final PremiumGrantRepository repository;

  public PremiumGrantQuery(PremiumGrantRepository repository) {
    this.repository = repository;
  }

  @Transactional(readOnly = true)
  public List<GrantView> forEnrollment(long enrollmentId) {
    return repository.findByEnrollmentIdOrderByCreatedAtDesc(enrollmentId).stream()
        .map(g -> new GrantView(g.getId(), g.getAction().name(), g.getStatus().name(), g.getAttempts(), g.getGrantedUntil(), g.getLastError()))
        .toList();
  }

  public record GrantView(long id, String action, String status, int attempts, Instant grantedUntil, String lastError) {}
}
