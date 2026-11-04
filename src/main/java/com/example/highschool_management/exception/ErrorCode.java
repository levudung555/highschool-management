package com.example.highschool_management.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
  UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
  USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
  UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
  UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
  AUTHENTICATION_FAILED(1008, "Authentication failed", HttpStatus.UNAUTHORIZED);

  ErrorCode(int code, String message, HttpStatus status) {
    this.code = code;
    this.message = message;
    this.status = status;
  }

  private final int code;
  private final String message;
  private final HttpStatus status;
}
