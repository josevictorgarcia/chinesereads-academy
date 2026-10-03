package com.chinesereads.academy.identity;

import static org.assertj.core.api.Assertions.assertThat;

import com.chinesereads.academy.identity.internal.SharedJwtVerifier;
import com.chinesereads.academy.support.ChineseReadsTokens;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/** Contrato JWT con ChineseReads: tokens emitidos con jjwt 0.11.5 y el fixture compartido de docs/. */
class SharedJwtVerifierContractTest {

  private final SharedJwtVerifier verifier = verifierFor(ChineseReadsTokens.TEST_SECRET);

  @Test
  void acceptsAnAccessTokenIssuedLikeChineseReadsDoes() {
    String token = ChineseReadsTokens.access("teacher@test.local");
    assertThat(verifier.verifyAccessToken(token)).contains("teacher@test.local");
  }

  @Test
  void rejectsRefreshTokens() {
    String token = ChineseReadsTokens.token(ChineseReadsTokens.TEST_SECRET, "x@test.local", "REFRESH", Duration.ofDays(7));
    assertThat(verifier.verifyAccessToken(token)).isEmpty();
  }

  @Test
  void rejectsExpiredTokens() {
    String token = ChineseReadsTokens.token(ChineseReadsTokens.TEST_SECRET, "x@test.local", "ACCESS", Duration.ofSeconds(-5));
    assertThat(verifier.verifyAccessToken(token)).isEmpty();
  }

  @Test
  void rejectsTokensSignedWithAnotherSecret() {
    String token = ChineseReadsTokens.token("another-secret-that-is-long-enough-0123456789", "x@test.local", "ACCESS", Duration.ofDays(1));
    assertThat(verifier.verifyAccessToken(token)).isEmpty();
  }

  @Test
  void rejectsGarbage() {
    assertThat(verifier.verifyAccessToken("not.a.jwt")).isEmpty();
    assertThat(verifier.verifyAccessToken("")).isEmpty();
    assertThat(verifier.verifyAccessToken(null)).isEmpty();
  }

  @Test
  void followsJjwtAlgorithmChoiceForLongSecrets() {
    // openssl rand -base64 48 produce 64 caracteres → jjwt firma con HS512; Academy debe aceptarlo igual.
    String secret64 = "A".repeat(64);
    String token = ChineseReadsTokens.token(secret64, "hs512@test.local", "ACCESS", Duration.ofDays(1));
    assertThat(verifierFor(secret64).verifyAccessToken(token)).contains("hs512@test.local");

    String secret48 = "B".repeat(48);
    String token384 = ChineseReadsTokens.token(secret48, "hs384@test.local", "ACCESS", Duration.ofDays(1));
    assertThat(verifierFor(secret48).verifyAccessToken(token384)).contains("hs384@test.local");
  }

  @Test
  void acceptsTheSharedFixtureFromDocs() throws IOException {
    Map<String, String> fixture = readFixture();
    SharedJwtVerifier fixtureVerifier = verifierFor(fixture.get("JWT_TEST_SECRET"));
    assertThat(fixtureVerifier.verifyAccessToken(fixture.get("JWT_FIXTURE_ACCESS"))).contains("teacher.fixture@example.com");
    assertThat(fixtureVerifier.verifyAccessToken(fixture.get("JWT_FIXTURE_REFRESH"))).isEmpty();
  }

  private static Map<String, String> readFixture() throws IOException {
    Path path = Paths.get("..", "docs", "integracion-chinesereads", "jwt-fixture.txt");
    return Files.readAllLines(path).stream()
        .filter(l -> !l.startsWith("#") && l.contains("="))
        .collect(Collectors.toMap(l -> l.substring(0, l.indexOf('=')), l -> l.substring(l.indexOf('=') + 1)));
  }

  private static SharedJwtVerifier verifierFor(String secret) {
    try {
      var ctor = SharedJwtVerifier.class.getDeclaredConstructor(String.class);
      ctor.setAccessible(true);
      return ctor.newInstance(secret);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException(e);
    }
  }
}
