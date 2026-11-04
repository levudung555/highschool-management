package com.example.highschool_management.exception;

public class AccessException extends RuntimeException {
  private final int status;

  public AccessException(int status, String message) {
    super(message);
    this.status = status;
  }

  public int getStatus() {
    return status;
  }
}
