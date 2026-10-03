package com.chinesereads.academy.identity.internal;

import com.chinesereads.academy.shared.config.ChineseReadsProperties;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

/**
 * Verifica los tokens emitidos por ChineseReads con el MISMO secreto. Reproduce la derivación de clave del
 * emisor (jjwt 0.11.5, {@code Keys.hmacShaKeyFor(secret.trim().getBytes(UTF_8))}): jjwt elige el algoritmo
 * HMAC más fuerte que permite la longitud del secreto (≥ 64 bytes → HS512, ≥ 48 → HS384, si no HS256), así que
 * aquí se hace exactamente lo mismo. Sin tolerancia de reloj, como el emisor.
 */
@Component
public class SharedJwtVerifier {

  private static final Logger log = LoggerFactory.getLogger(SharedJwtVerifier.class);
  public static final String ACCESS_TYPE = "ACCESS";

  private final NimbusJwtDecoder decoder;

  @Autowired
  public SharedJwtVerifier(ChineseReadsProperties properties) {
    this(properties.jwtSecret());
  }

  SharedJwtVerifier(String secret) {
    byte[] keyBytes = secret.trim().getBytes(StandardCharsets.UTF_8);
    MacAlgorithm algorithm = algorithmFor(keyBytes.length);
    SecretKey key = new SecretKeySpec(keyBytes, "Hmac" + algorithm.getName().replace("HS", "SHA"));
    this.decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(algorithm).build();
    this.decoder.setJwtValidator(new JwtTimestampValidator(Duration.ZERO));
  }

  static MacAlgorithm algorithmFor(int keyLengthBytes) {
    if (keyLengthBytes >= 64) {
      return MacAlgorithm.HS512;
    }
    if (keyLengthBytes >= 48) {
      return MacAlgorithm.HS384;
    }
    return MacAlgorithm.HS256;
  }

  /** Devuelve el email del usuario si el token es un ACCESS token válido y vigente; vacío en cualquier otro caso. */
  public Optional<String> verifyAccessToken(String token) {
    if (token == null || token.isBlank()) {
      return Optional.empty();
    }
    try {
      Jwt jwt = decoder.decode(token);
      if (!ACCESS_TYPE.equals(jwt.getClaimAsString("type"))) {
        log.debug("Rejected shared token: type is not ACCESS");
        return Optional.empty();
      }
      String subject = jwt.getSubject();
      return subject == null || subject.isBlank() ? Optional.empty() : Optional.of(subject);
    } catch (JwtException ex) {
      // Sin datos personales en el log: solo el motivo técnico.
      log.debug("Rejected shared token: {}", ex.getMessage());
      return Optional.empty();
    }
  }
}
