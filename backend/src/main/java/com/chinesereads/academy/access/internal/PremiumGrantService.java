package com.chinesereads.academy.access.internal;

import com.chinesereads.academy.access.PremiumAccessException;
import com.chinesereads.academy.access.PremiumAccessPort;
import java.time.Clock;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Ejecuta una fila de premium_grant contra ChineseReads y deja el resultado en la propia fila. */
@Service
public class PremiumGrantService {

  private static final Logger log = LoggerFactory.getLogger(PremiumGrantService.class);

  private final PremiumGrantRepository repository;
  private final PremiumAccessPort port;
  private final Clock clock;

  public PremiumGrantService(PremiumGrantRepository repository, PremiumAccessPort port, Clock clock) {
    this.repository = repository;
    this.port = port;
    this.clock = clock;
  }

  /** Reintento desde el job: carga la fila en su propia transacción y la ejecuta. */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public PremiumGrant retry(long grantId) {
    return execute(repository.findById(grantId).orElseThrow());
  }

  /**
   * Ejecuta una fila ya persistida dentro de la transacción del llamador (el listener de Modulith). La llamada
   * HTTP nunca lanza aquí: el resultado DONE/FAILED queda en la fila y, si falla, el job lo reintenta.
   */
  @Transactional
  public PremiumGrant execute(PremiumGrant grant) {
    Instant now = Instant.now(clock);
    try {
      if (grant.getAction() == PremiumGrant.Action.GRANT) {
        port.grant(grant.getStudentUserId(), grant.getGrantedUntil(), grant.reference());
      } else {
        port.revoke(grant.getStudentUserId(), grant.reference());
      }
      grant.markDone(now);
      log.info("premium {} done for userId={} ({})", grant.getAction(), grant.getStudentUserId(), grant.reference());
    } catch (PremiumAccessException ex) {
      grant.markFailed(ex.getMessage(), now);
      log.warn("premium {} FAILED for userId={} attempt={} : {}", grant.getAction(), grant.getStudentUserId(),
          grant.getAttempts(), ex.getMessage());
    }
    return repository.save(grant);
  }
}
