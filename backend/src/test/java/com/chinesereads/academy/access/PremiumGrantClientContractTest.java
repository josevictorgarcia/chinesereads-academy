package com.chinesereads.academy.access;

import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.chinesereads.academy.access.internal.ChineseReadsPremiumClient;
import com.chinesereads.academy.shared.config.ChineseReadsProperties;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.time.Instant;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Contrato del endpoint interno (CONTRACT.md §3) contra los MISMOS mappings WireMock que usa el entorno
 * local, así el stub de desarrollo y el test no pueden divergir.
 */
class PremiumGrantClientContractTest {

  static final String DEV_SECRET = "dev-internal-secret-change-me-0123456789abcdef";
  static WireMockServer server;

  @BeforeAll
  static void start() throws IOException {
    // WireMock espera <root>/mappings/*.json: se copian los ficheros del contrato a un directorio temporal.
    Path root = Files.createTempDirectory("academy-wiremock");
    Path mappings = Files.createDirectories(root.resolve("mappings"));
    Path source = Paths.get("..", "docs", "integracion-chinesereads", "wiremock").toAbsolutePath().normalize();
    try (Stream<Path> files = Files.list(source)) {
      files.filter(f -> f.toString().endsWith(".json"))
          .forEach(f -> {
            try {
              Files.copy(f, mappings.resolve(f.getFileName()));
            } catch (IOException e) {
              throw new IllegalStateException(e);
            }
          });
    }
    server = new WireMockServer(WireMockConfiguration.options().dynamicPort().globalTemplating(true)
        .usingFilesUnderDirectory(root.toString()));
    server.start();
  }

  @AfterAll
  static void stop() {
    server.stop();
  }

  private static ChineseReadsPremiumClient client(String secret) {
    return new ChineseReadsPremiumClient(new ChineseReadsProperties("x".repeat(40), "http://localhost:4200",
        new ChineseReadsProperties.Internal(server.baseUrl(), secret)));
  }

  @Test
  void grantsPremiumWithTheServiceSecretAndTheContractBody() {
    Instant until = Instant.parse("2027-10-02T00:00:00Z");
    assertThatCode(() -> client(DEV_SECRET).grant(42L, until, "enrollment:7")).doesNotThrowAnyException();
    server.verify(postRequestedFor(urlPathEqualTo("/api/internal/premium-grant"))
        .withHeader("X-Service-Secret", equalTo(DEV_SECRET))
        .withHeader("X-Request-Id", equalTo("n/a"))
        .withRequestBody(matchingJsonPath("$.userId", equalTo("42")))
        .withRequestBody(matchingJsonPath("$.until", equalTo("2027-10-02T00:00:00Z")))
        .withRequestBody(matchingJsonPath("$.source", equalTo("academy")))
        .withRequestBody(matchingJsonPath("$.reference", equalTo("enrollment:7"))));
  }

  @Test
  void revokesPremiumWithTheSourceParameter() {
    assertThatCode(() -> client(DEV_SECRET).revoke(42L, "enrollment:7")).doesNotThrowAnyException();
    server.verify(deleteRequestedFor(urlEqualTo("/api/internal/premium-grant/42?source=academy"))
        .withHeader("X-Service-Secret", equalTo(DEV_SECRET)));
  }

  @Test
  void aWrongSecretIsRejectedWith401() {
    assertThatThrownBy(() -> client("wrong-secret").grant(42L, Instant.now().plusSeconds(60), "enrollment:1"))
        .isInstanceOf(PremiumAccessException.class)
        .hasMessageContaining("401");
  }

  @Test
  void anUnreachableChineseReadsIsReportedNotSwallowed() {
    var unreachable = new ChineseReadsPremiumClient(new ChineseReadsProperties("x".repeat(40), "http://localhost:4200",
        new ChineseReadsProperties.Internal("http://localhost:1", DEV_SECRET)));
    assertThatThrownBy(() -> unreachable.revoke(1L, "enrollment:1")).isInstanceOf(PremiumAccessException.class);
  }
}
