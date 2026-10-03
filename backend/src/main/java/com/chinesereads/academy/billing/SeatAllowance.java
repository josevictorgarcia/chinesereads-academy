package com.chinesereads.academy.billing;

import java.time.Instant;

/** Lo que classroom necesita saber de la suscripción de un profesor para activar un asiento. */
public record SeatAllowance(long teacherId, int seatsIncluded, int seatsExtra, Instant periodEnd, boolean active) {

  public int totalSeats() {
    return seatsIncluded + seatsExtra;
  }
}
