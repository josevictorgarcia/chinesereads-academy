package com.chinesereads.academy.identity.internal;

import com.chinesereads.academy.shared.error.ErrorCode;
import com.chinesereads.academy.shared.web.Problems;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Defensa en profundidad contra CSRF con la cookie compartida: toda mutación bajo /api exige la cabecera
 * {@code X-Requested-With: XMLHttpRequest}, que un formulario HTML de otro sitio no puede añadir.
 * Exentas: rutas públicas y webhooks (firmados por su proveedor).
 */
public class XhrHeaderFilter extends OncePerRequestFilter {

  public static final String HEADER = "X-Requested-With";
  public static final String EXPECTED = "XMLHttpRequest";
  private static final Set<String> MUTATING = Set.of("POST", "PUT", "PATCH", "DELETE");

  private final List<String> exemptPrefixes;

  public XhrHeaderFilter(List<String> exemptPrefixes) {
    this.exemptPrefixes = exemptPrefixes;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String path = request.getRequestURI();
    if (MUTATING.contains(request.getMethod()) && path.startsWith("/api/") && !isExempt(path)
        && !EXPECTED.equals(request.getHeader(HEADER))) {
      Problems.write(response, ErrorCode.XHR_HEADER_REQUIRED, "Header " + HEADER + ": " + EXPECTED + " is required");
      return;
    }
    chain.doFilter(request, response);
  }

  private boolean isExempt(String path) {
    return exemptPrefixes.stream().anyMatch(path::startsWith);
  }
}
