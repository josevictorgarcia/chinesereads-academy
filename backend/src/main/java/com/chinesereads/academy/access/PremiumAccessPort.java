package com.chinesereads.academy.access;

import java.time.Instant;

/** Puerto hacia ChineseReads. Implementaciones: cliente HTTP real (producción/dev con stub) o no-op (tests). */
public interface PremiumAccessPort {

  /** Concede premium hasta {@code until}. Idempotente. Lanza {@link PremiumAccessException} si no se pudo aplicar. */
  void grant(long userId, Instant until, String reference);

  /** Retira el premium concedido por Academy. Idempotente. */
  void revoke(long userId, String reference);
}
