package com.chinesereads.academy.identity.internal;

import com.chinesereads.academy.identity.AcademyRole;
import com.chinesereads.academy.identity.CurrentUser;
import com.chinesereads.academy.identity.RoleContributor;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Lee la cookie {@code AuthToken} de ChineseReads, la verifica y carga el usuario del esquema matriz.
 * Sin cookie, token inválido, usuario inexistente o bloqueado → la petición sigue anónima (los endpoints
 * protegidos responden 401). Los roles de Academy los aportan los {@link RoleContributor} de otros módulos.
 */
public class SharedJwtAuthenticationFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(SharedJwtAuthenticationFilter.class);
  public static final String COOKIE_NAME = "AuthToken";

  private final SharedJwtVerifier verifier;
  private final ChineseReadsUserLookup users;
  private final List<RoleContributor> roleContributors;

  public SharedJwtAuthenticationFilter(SharedJwtVerifier verifier, ChineseReadsUserLookup users,
      List<RoleContributor> roleContributors) {
    this.verifier = verifier;
    this.users = users;
    this.roleContributors = roleContributors;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    readCookie(request)
        .flatMap(verifier::verifyAccessToken)
        .flatMap(users::byEmail)
        .ifPresent(user -> {
          if (user.blocked()) {
            log.info("Rejected a blocked account on {}", request.getRequestURI());
            return;
          }
          Set<AcademyRole> roles = roleContributors.stream()
              .flatMap(c -> c.rolesFor(user.id()).stream())
              .collect(Collectors.toCollection(() -> EnumSet.noneOf(AcademyRole.class)));
          CurrentUser principal = new CurrentUser(user.id(), user.email(), user.name(), user.language(), Set.copyOf(roles));
          List<GrantedAuthority> authorities = Stream.concat(
                  Stream.of("ROLE_USER"), roles.stream().map(AcademyRole::authority))
              .map(SimpleGrantedAuthority::new)
              .map(GrantedAuthority.class::cast)
              .toList();
          var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
          authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authentication);
        });
    chain.doFilter(request, response);
  }

  private static Optional<String> readCookie(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return Optional.empty();
    }
    for (Cookie cookie : cookies) {
      if (COOKIE_NAME.equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
        return Optional.of(cookie.getValue());
      }
    }
    return Optional.empty();
  }
}
