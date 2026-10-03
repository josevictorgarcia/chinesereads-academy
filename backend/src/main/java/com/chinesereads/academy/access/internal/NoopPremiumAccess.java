package com.chinesereads.academy.access.internal;

import com.chinesereads.academy.access.PremiumAccessPort;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Sustituto cuando academy.access.enabled=false: solo registra. Para tests sin stub. */
@Component
@ConditionalOnProperty(name = "academy.access.enabled", havingValue = "false")
public class NoopPremiumAccess implements PremiumAccessPort {

  private static final Logger log = LoggerFactory.getLogger(NoopPremiumAccess.class);

  @Override
  public void grant(long userId, Instant until, String reference) {
    log.info("[noop] premium grant userId={} until={} ref={}", userId, until, reference);
  }

  @Override
  public void revoke(long userId, String reference) {
    log.info("[noop] premium revoke userId={} ref={}", userId, reference);
  }
}
