package com.chinesereads.academy.billing;

import com.chinesereads.academy.billing.internal.TeacherSubscriptionRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** API pública de billing hacia classroom: cuota de asientos vigente de un profesor. */
@Service
public class SeatQuotaService {

  private final TeacherSubscriptionRepository repository;
  private final Clock clock;

  public SeatQuotaService(TeacherSubscriptionRepository repository, Clock clock) {
    this.repository = repository;
    this.clock = clock;
  }

  @Transactional(readOnly = true)
  public Optional<SeatAllowance> allowanceFor(long teacherId) {
    Instant now = Instant.now(clock);
    return repository.findByTeacherId(teacherId).map(s -> new SeatAllowance(
        teacherId, s.getSeatsIncluded(), s.getSeatsExtra(), s.getCurrentPeriodEnd(), s.isActiveAt(now)));
  }

  /** Vista para el panel del profesor. */
  @Transactional(readOnly = true)
  public Optional<SubscriptionView> subscriptionOf(long teacherId) {
    Instant now = Instant.now(clock);
    return repository.findByTeacherId(teacherId).map(s -> new SubscriptionView(
        s.getSource().name(), s.getStatus().name(), s.getSeatsIncluded() + s.getSeatsExtra(),
        s.getCurrentPeriodEnd(), s.isActiveAt(now)));
  }

  public record SubscriptionView(String source, String status, int totalSeats, Instant periodEnd, boolean active) {}

}
