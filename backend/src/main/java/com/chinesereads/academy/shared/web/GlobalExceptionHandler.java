package com.chinesereads.academy.shared.web;

import com.chinesereads.academy.shared.error.AcademyException;
import com.chinesereads.academy.shared.error.ErrorCode;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Único punto de conversión de excepciones a ProblemDetail. Los 5xx se registran con su {@code errorId}
 * para que el mensaje que ve el usuario permita localizar el log exacto.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(AcademyException.class)
  public ProblemDetail handleAcademy(AcademyException ex) {
    String errorId = Problems.newErrorId();
    log.info("{} errorId={} detail={}", ex.code(), errorId, ex.getMessage());
    return Problems.of(ex.code(), ex.getMessage(), errorId);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
    List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
        .map(fe -> Map.of("field", fe.getField(), "message", String.valueOf(fe.getDefaultMessage())))
        .toList();
    ProblemDetail problem = Problems.of(ErrorCode.VALIDATION_FAILED, "Request validation failed", Problems.newErrorId());
    problem.setProperty("errors", errors);
    return problem;
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ProblemDetail handleUnreadable(HttpMessageNotReadableException ex) {
    return Problems.of(ErrorCode.MALFORMED_REQUEST, "Malformed request body", Problems.newErrorId());
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ProblemDetail handleNotFound(NoResourceFoundException ex) {
    return Problems.of(ErrorCode.NOT_FOUND, "No such resource", Problems.newErrorId());
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ProblemDetail handleMethod(HttpRequestMethodNotSupportedException ex) {
    return Problems.of(ErrorCode.METHOD_NOT_ALLOWED, ex.getMessage(), Problems.newErrorId());
  }

  @ExceptionHandler(AuthorizationDeniedException.class)
  public ProblemDetail handleDenied(AuthorizationDeniedException ex) {
    return Problems.of(ErrorCode.FORBIDDEN, "Access denied", Problems.newErrorId());
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleUnexpected(Exception ex) {
    String errorId = Problems.newErrorId();
    log.error("Unexpected error errorId={} correlationId={}", errorId, CorrelationIdFilter.current(), ex);
    return Problems.of(ErrorCode.INTERNAL_ERROR, "Unexpected error. Reference: " + errorId, errorId);
  }
}
