package com.chinesereads.academy.support;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.crypto.SecretKey;

/**
 * Emite tokens EXACTAMENTE como ChineseReads (jjwt 0.11.5, misma derivación de clave y mismos claims),
 * para que los tests de contrato prueben la compatibilidad real y no una suposición.
 */
public final class ChineseReadsTokens {

  public static final String TEST_SECRET = "academy-contract-test-secret-0123456789abcdef";

  private ChineseReadsTokens() {}

  public static String access(String email) {
    return token(TEST_SECRET, email, "ACCESS", Duration.ofDays(7));
  }

  public static String token(String secret, String email, String type, Duration ttl) {
    SecretKey key = Keys.hmacShaKeyFor(secret.trim().getBytes(StandardCharsets.UTF_8));
    Instant now = Instant.now();
    return Jwts.builder()
        .setSubject(email)
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(now.plus(ttl)))
        .claim("roles", List.of(Map.of("authority", "ROLE_USER")))
        .claim("type", type)
        .signWith(key)
        .compact();
  }
}
