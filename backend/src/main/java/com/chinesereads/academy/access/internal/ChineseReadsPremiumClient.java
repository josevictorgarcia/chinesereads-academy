package com.chinesereads.academy.access.internal;

import com.chinesereads.academy.access.PremiumAccessException;
import com.chinesereads.academy.access.PremiumAccessPort;
import com.chinesereads.academy.shared.config.ChineseReadsProperties;
import com.chinesereads.academy.shared.web.CorrelationIdFilter;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.ResourceAccessException;

/**
 * Cliente HTTP del endpoint interno de ChineseReads (CONTRACT.md §3). Timeouts cortos; propaga X-Request-Id.
 * Activo salvo que academy.access.enabled=false (tests sin WireMock).
 */
@Component
@ConditionalOnProperty(name = "academy.access.enabled", havingValue = "true", matchIfMissing = true)
public class ChineseReadsPremiumClient implements PremiumAccessPort {

  private static final Logger log = LoggerFactory.getLogger(ChineseReadsPremiumClient.class);
  static final String SECRET_HEADER = "X-Service-Secret";

  private final RestClient client;
  private final String secret;

  public ChineseReadsPremiumClient(ChineseReadsProperties properties) {
    // HTTP/1.1 explícito: el backend de ChineseReads (Tomcat) y el stub WireMock hablan HTTP/1.1; la negociación
    // HTTP/2 por defecto del cliente JDK provoca cortes de conexión contra ellos.
    HttpClient httpClient = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_1_1)
        .connectTimeout(Duration.ofSeconds(3))
        .build();
    var factory = new JdkClientHttpRequestFactory(httpClient);
    factory.setReadTimeout(Duration.ofSeconds(10));
    this.client = RestClient.builder()
        .baseUrl(properties.internal().baseUrl())
        .requestFactory(factory)
        .build();
    this.secret = properties.internal().serviceSecret() == null ? "" : properties.internal().serviceSecret();
  }

  @Override
  public void grant(long userId, Instant until, String reference) {
    execute("grant", () -> client.post()
        .uri("/api/internal/premium-grant")
        .header(SECRET_HEADER, secret)
        .header(CorrelationIdFilter.HEADER, CorrelationIdFilter.current())
        .contentType(MediaType.APPLICATION_JSON)
        .body(Map.of("userId", userId, "until", until.toString(), "source", "academy", "reference", reference))
        .retrieve()
        .toBodilessEntity());
  }

  @Override
  public void revoke(long userId, String reference) {
    execute("revoke", () -> client.delete()
        .uri("/api/internal/premium-grant/{userId}?source=academy", userId)
        .header(SECRET_HEADER, secret)
        .header(CorrelationIdFilter.HEADER, CorrelationIdFilter.current())
        .retrieve()
        .toBodilessEntity());
  }

  private void execute(String op, Runnable call) {
    try {
      call.run();
    } catch (RestClientResponseException ex) {
      // El cuerpo puede llevar datos; solo se registra el estado.
      log.warn("premium-grant {} rejected by ChineseReads: HTTP {}", op, ex.getStatusCode().value());
      throw new PremiumAccessException(op + " rejected with HTTP " + ex.getStatusCode().value(), ex);
    } catch (ResourceAccessException ex) {
      log.warn("premium-grant {} unreachable: {}", op, ex.getMessage());
      throw new PremiumAccessException(op + " unreachable: " + ex.getMessage(), ex);
    }
  }
}
