package com.chinesereads.academy.shared.error;

import org.springframework.http.HttpStatus;

/**
 * Códigos estables que la interfaz traduce (clave {@code errors.<CODE>}). El texto {@code detail} de la
 * respuesta es solo para desarrollo; nunca se muestra tal cual al usuario.
 */
public enum ErrorCode {
  UNAUTHENTICATED(HttpStatus.UNAUTHORIZED),
  FORBIDDEN(HttpStatus.FORBIDDEN),
  XHR_HEADER_REQUIRED(HttpStatus.FORBIDDEN),
  VALIDATION_FAILED(HttpStatus.BAD_REQUEST),
  MALFORMED_REQUEST(HttpStatus.BAD_REQUEST),
  NOT_FOUND(HttpStatus.NOT_FOUND),
  METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED),
  INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),

  IDENTITY_TEACHER_ALREADY_REGISTERED(HttpStatus.CONFLICT),
  IDENTITY_TEACHER_REQUIRED(HttpStatus.FORBIDDEN);

  private final HttpStatus status;

  ErrorCode(HttpStatus status) {
    this.status = status;
  }

  public HttpStatus status() {
    return status;
  }
}
