package com.chinesereads.academy.access;

public class PremiumAccessException extends RuntimeException {

  public PremiumAccessException(String message, Throwable cause) {
    super(message, cause);
  }

  public PremiumAccessException(String message) {
    super(message);
  }
}
