package com.chinesereads.academy.access.internal;

import com.chinesereads.academy.classroom.SeatActivated;
import com.chinesereads.academy.classroom.SeatDeactivated;
import java.time.Clock;
import java.time.Instant;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Convierte los eventos de asiento en filas de premium_grant y las ejecuta. Si la llamada falla, la fila queda
 * FAILED y el job de reintentos la retoma; el evento se da por consumido (Modulith) porque el estado ya está en BD.
 */
@Component
public class SeatEventsListener {

  private final PremiumGrantRepository repository;
  private final PremiumGrantService service;
  private final Clock clock;

  public SeatEventsListener(PremiumGrantRepository repository, PremiumGrantService service, Clock clock) {
    this.repository = repository;
    this.service = service;
    this.clock = clock;
  }

  @ApplicationModuleListener
  public void on(SeatActivated event) {
    PremiumGrant grant = repository.save(new PremiumGrant(event.enrollmentId(), event.studentUserId(),
        PremiumGrant.Action.GRANT, event.premiumUntil(), Instant.now(clock)));
    service.execute(grant);
  }

  @ApplicationModuleListener
  public void on(SeatDeactivated event) {
    PremiumGrant grant = repository.save(new PremiumGrant(event.enrollmentId(), event.studentUserId(),
        PremiumGrant.Action.REVOKE, null, Instant.now(clock)));
    service.execute(grant);
  }
}
