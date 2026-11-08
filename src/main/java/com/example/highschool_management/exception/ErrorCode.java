package com.example.highschool_management.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized Exception", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_EXISTED(1001, "User existed", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(1002, "Email existed", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1003, "User Not Existed", HttpStatus.BAD_REQUEST),
    AUTHENTICATION_FAILED(1004, "Authentication Failed", HttpStatus.UNAUTHORIZED),
    INVALID_KEY(1008, "Invalid input", HttpStatus.BAD_REQUEST),
    ROLE_NOT_FOUND(1031, "Role not found", HttpStatus.NOT_FOUND),
    UNAUTHORIZED(1032, "You do not have permission", HttpStatus.FORBIDDEN),
    ACCOUNT_DISABLED(1041, "Account is disabled", HttpStatus.FORBIDDEN);

    ErrorCode(int code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    private final int code;
    private final String message;
    private final HttpStatus status;
}
