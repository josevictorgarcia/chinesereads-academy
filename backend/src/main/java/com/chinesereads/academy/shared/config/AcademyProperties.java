package com.chinesereads.academy.shared.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "academy")
public record AcademyProperties(@NotBlank String frontendUrl) {}
