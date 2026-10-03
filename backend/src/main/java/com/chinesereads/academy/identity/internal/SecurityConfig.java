package com.chinesereads.academy.identity.internal;

import com.chinesereads.academy.identity.RoleContributor;
import com.chinesereads.academy.shared.error.ErrorCode;
import com.chinesereads.academy.shared.web.Problems;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

/**
 * Deny by default: todo exige sesión salvo lo listado explícitamente. Sin formularios, sin sesión HTTP,
 * sin CSRF (API sin estado con cookie HttpOnly + cabecera XHR obligatoria en mutaciones).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  /** Prefijos públicos: no requieren cookie ni cabecera XHR. Los webhooks verifican su propia firma. */
  public static final List<String> PUBLIC_PREFIXES = List.of("/api/public/", "/api/billing/webhook");

  @Bean
  SecurityFilterChain apiSecurity(HttpSecurity http, SharedJwtVerifier verifier, ChineseReadsUserLookup users,
      List<RoleContributor> roleContributors) throws Exception {
    var jwtFilter = new SharedJwtAuthenticationFilter(verifier, users, roleContributors);
    var xhrFilter = new XhrHeaderFilter(PUBLIC_PREFIXES);
    http
        .csrf(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .logout(AbstractHttpConfigurer::disable)
        .anonymous(Customizer.withDefaults())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/public/**", "/api/billing/webhook", "/actuator/**", "/error").permitAll()
            .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
            .anyRequest().authenticated())
        .exceptionHandling(e -> e
            .authenticationEntryPoint((req, res, ex) ->
                Problems.write(res, ErrorCode.UNAUTHENTICATED, "Authentication required"))
            .accessDeniedHandler((req, res, ex) ->
                Problems.write(res, ErrorCode.FORBIDDEN, "Access denied")))
        .addFilterBefore(jwtFilter, AuthorizationFilter.class)
        .addFilterBefore(xhrFilter, SharedJwtAuthenticationFilter.class);
    return http.build();
  }
}
