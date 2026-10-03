package com.chinesereads.academy.shared.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Todo lo que Academy necesita saber del proyecto matriz. Se valida al arrancar: sin un secreto JWT
 * de al menos 32 bytes la aplicación no levanta, porque sin él la sesión compartida fallaría en silencio.
 */
@Validated
@ConfigurationProperties(prefix = "chinesereads")
public record ChineseReadsProperties(
    @NotBlank(message = "JWT_SECRET es obligatorio: debe ser el mismo valor que usa ChineseReads")
    @Size(min = 32, message = "JWT_SECRET debe tener al menos 32 bytes (openssl rand -base64 48)")
    String jwtSecret,
    @NotBlank String publicUrl,
    @NotNull @Valid Internal internal) {

  /** Endpoint interno de ChineseReads (red Docker). El secreto se exige cuando el módulo access esté activo. */
  public record Internal(@NotBlank String baseUrl, String serviceSecret) {}
}
