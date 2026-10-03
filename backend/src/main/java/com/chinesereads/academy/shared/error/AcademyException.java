package com.chinesereads.academy.shared.error;

/** Excepción de dominio: lleva el código estable y un detalle legible para desarrollo. */
public class AcademyException extends RuntimeException {

  private final ErrorCode code;

  public AcademyException(ErrorCode code, String detail) {
    super(detail);
    this.code = code;
  }

  public AcademyException(ErrorCode code) {
    this(code, code.name());
  }

  public ErrorCode code() {
    return code;
  }
}
