package com.chinesereads.academy.shared.web;

import com.chinesereads.academy.shared.error.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.security.SecureRandom;
import java.util.HexFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;

/**
 * Construye respuestas de error RFC 9457 con las extensiones de Academy: {@code code} (estable),
 * {@code errorId} (para localizar el log) y {@code correlationId}.
 */
public final class Problems {

  private static final SecureRandom RANDOM = new SecureRandom();
  private static final HexFormat HEX = HexFormat.of();

  private Problems() {}

  public static String newErrorId() {
    byte[] bytes = new byte[4];
    RANDOM.nextBytes(bytes);
    return HEX.formatHex(bytes);
  }

  public static ProblemDetail of(ErrorCode code, String detail, String errorId) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(code.status(), detail);
    problem.setTitle(code.status().getReasonPhrase());
    problem.setType(URI.create("/errors/" + code.name()));
    problem.setProperty("code", code.name());
    problem.setProperty("errorId", errorId);
    problem.setProperty("correlationId", CorrelationIdFilter.current());
    return problem;
  }

  /**
   * Escribe un ProblemDetail desde un filtro (fuera de Spring MVC, donde no actúa el manejador global).
   * Se serializa a mano: valores controlados por nosotros, sin dependencia del mapeador JSON.
   */
  public static void write(HttpServletResponse response, ErrorCode code, String detail) throws IOException {
    String errorId = newErrorId();
    response.setStatus(code.status().value());
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    String body = "{"
        + "\"type\":\"/errors/" + code.name() + "\","
        + "\"title\":" + json(code.status().getReasonPhrase()) + ","
        + "\"status\":" + code.status().value() + ","
        + "\"detail\":" + json(detail) + ","
        + "\"code\":\"" + code.name() + "\","
        + "\"errorId\":\"" + errorId + "\","
        + "\"correlationId\":" + json(CorrelationIdFilter.current())
        + "}";
    response.getWriter().write(body);
  }

  private static String json(String value) {
    if (value == null) {
      return "null";
    }
    StringBuilder sb = new StringBuilder("\"");
    for (char c : value.toCharArray()) {
      switch (c) {
        case '"' -> sb.append("\\\"");
        case '\\' -> sb.append("\\\\");
        case '\n' -> sb.append("\\n");
        case '\r' -> sb.append("\\r");
        case '\t' -> sb.append("\\t");
        default -> {
          if (c < 0x20) {
            sb.append(String.format("\\u%04x", (int) c));
          } else {
            sb.append(c);
          }
        }
      }
    }
    return sb.append('"').toString();
  }
}
