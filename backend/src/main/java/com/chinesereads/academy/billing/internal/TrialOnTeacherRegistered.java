package com.chinesereads.academy.billing.internal;

import com.chinesereads.academy.identity.TeacherRegistered;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Al darse de alta un profesor arranca un periodo de prueba (sin Stripe). Listener de Modulith:
 * se ejecuta tras el commit del alta, en transacción propia, y queda registrado para reintentar si falla.
 */
@Component
public class TrialOnTeacherRegistered {

  private static final Logger log = LoggerFactory.getLogger(TrialOnTeacherRegistered.class);

  private final TeacherSubscriptionRepository repository;
  private final Clock clock;
  private final int trialSeats;
  private final Duration trialDuration;

  public TrialOnTeacherRegistered(TeacherSubscriptionRepository repository, Clock clock,
      @Value("${academy.billing.trial-seats:10}") int trialSeats,
      @Value("${academy.billing.trial-duration:P30D}") Duration trialDuration) {
    this.repository = repository;
    this.clock = clock;
    this.trialSeats = trialSeats;
    this.trialDuration = trialDuration;
  }

  @ApplicationModuleListener
  public void on(TeacherRegistered event) {
    if (repository.findByTeacherId(event.teacherId()).isPresent()) {
      return;
    }
    Instant now = Instant.now(clock);
    repository.save(TeacherSubscription.trial(event.teacherId(), trialSeats, now, now.plus(trialDuration)));
    log.info("Trial subscription created for teacherId={} ({} seats, {} days)", event.teacherId(), trialSeats,
        trialDuration.toDays());
  }
}
