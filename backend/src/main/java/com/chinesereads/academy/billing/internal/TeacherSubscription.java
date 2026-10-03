package com.chinesereads.academy.billing.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "teacher_subscription")
public class TeacherSubscription {

  public enum Source { STRIPE, MANUAL, TRIAL }

  public enum Status { TRIALING, ACTIVE, PAST_DUE, CANCELED, EXPIRED }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "teacher_id", nullable = false, unique = true)
  private long teacherId;

  @Enumerated(EnumType.STRING)
  @Column(name = "source", nullable = false, length = 16)
  private Source source;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 16)
  private Status status;

  @Column(name = "stripe_customer_id", length = 64)
  private String stripeCustomerId;

  @Column(name = "stripe_subscription_id", length = 64)
  private String stripeSubscriptionId;

  @Column(name = "stripe_price_id", length = 64)
  private String stripePriceId;

  @Column(name = "seats_included", nullable = false)
  private int seatsIncluded;

  @Column(name = "seats_extra", nullable = false)
  private int seatsExtra;

  @Column(name = "current_period_end", nullable = false)
  private Instant currentPeriodEnd;

  @Column(name = "cancel_at_period_end", nullable = false)
  private boolean cancelAtPeriodEnd;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected TeacherSubscription() {}

  public static TeacherSubscription trial(long teacherId, int seats, Instant now, Instant periodEnd) {
    TeacherSubscription s = new TeacherSubscription();
    s.teacherId = teacherId;
    s.source = Source.TRIAL;
    s.status = Status.TRIALING;
    s.seatsIncluded = seats;
    s.seatsExtra = 0;
    s.currentPeriodEnd = periodEnd;
    s.createdAt = now;
    s.updatedAt = now;
    return s;
  }

  public boolean isActiveAt(Instant now) {
    return (status == Status.TRIALING || status == Status.ACTIVE) && currentPeriodEnd.isAfter(now);
  }

  public Long getId() { return id; }
  public long getTeacherId() { return teacherId; }
  public Source getSource() { return source; }
  public Status getStatus() { return status; }
  public String getStripeCustomerId() { return stripeCustomerId; }
  public String getStripeSubscriptionId() { return stripeSubscriptionId; }
  public String getStripePriceId() { return stripePriceId; }
  public int getSeatsIncluded() { return seatsIncluded; }
  public int getSeatsExtra() { return seatsExtra; }
  public Instant getCurrentPeriodEnd() { return currentPeriodEnd; }
  public boolean isCancelAtPeriodEnd() { return cancelAtPeriodEnd; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
}
