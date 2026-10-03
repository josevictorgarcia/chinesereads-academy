package com.chinesereads.academy.access.internal;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Reintenta las concesiones PENDING/FAILED (p. ej. ChineseReads caído) hasta un máximo de intentos. */
@Component
public class PremiumGrantRetryJob {

  private static final Logger log = LoggerFactory.getLogger(PremiumGrantRetryJob.class);

  private final PremiumGrantRepository repository;
  private final PremiumGrantService service;
  private final int maxAttempts;

  public PremiumGrantRetryJob(PremiumGrantRepository repository, PremiumGrantService service,
      @Value("${academy.access.max-attempts:20}") int maxAttempts) {
    this.repository = repository;
    this.service = service;
    this.maxAttempts = maxAttempts;
  }

  @Scheduled(fixedDelayString = "${academy.access.retry-delay:PT5M}", initialDelayString = "${academy.access.retry-initial-delay:PT1M}")
  public void retryPending() {
    List<PremiumGrant> pending = repository.findByStatusInAndAttemptsLessThanOrderByUpdatedAtAsc(
        List.of(PremiumGrant.Status.PENDING, PremiumGrant.Status.FAILED), maxAttempts);
    if (pending.isEmpty()) {
      return;
    }
    log.info("Retrying {} premium grant(s)", pending.size());
    pending.forEach(g -> service.retry(g.getId()));
  }
}
