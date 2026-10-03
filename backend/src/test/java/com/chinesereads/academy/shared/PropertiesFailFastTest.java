package com.chinesereads.academy.shared;

import static org.assertj.core.api.Assertions.assertThat;

import com.chinesereads.academy.shared.config.ChineseReadsProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.validation.autoconfigure.ValidationAutoConfiguration;
import org.springframework.context.annotation.Configuration;

/** Sin JWT_SECRET (o demasiado corto) la aplicación no debe arrancar: un secreto ausente sería un fallo silencioso. */
class PropertiesFailFastTest {

  private final ApplicationContextRunner runner = new ApplicationContextRunner()
      .withConfiguration(AutoConfigurations.of(ValidationAutoConfiguration.class))
      .withUserConfiguration(Config.class)
      .withPropertyValues("chinesereads.public-url=https://chinesereads.com", "chinesereads.internal.base-url=http://backend:8080");

  @Test
  void failsWithoutSecret() {
    runner.run(ctx -> assertThat(ctx).hasFailed());
  }

  @Test
  void failsWithShortSecret() {
    runner.withPropertyValues("chinesereads.jwt-secret=too-short").run(ctx -> assertThat(ctx).hasFailed());
  }

  @Test
  void startsWithAProperSecret() {
    runner.withPropertyValues("chinesereads.jwt-secret=academy-contract-test-secret-0123456789abcdef")
        .run(ctx -> {
          assertThat(ctx).hasNotFailed();
          assertThat(ctx.getBean(ChineseReadsProperties.class).internal().baseUrl()).isEqualTo("http://backend:8080");
        });
  }

  @Configuration
  @EnableConfigurationProperties(ChineseReadsProperties.class)
  static class Config {}
}
