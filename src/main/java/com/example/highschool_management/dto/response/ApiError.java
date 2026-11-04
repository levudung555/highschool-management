package com.example.highschool_management.dto.response;

public record ApiError(
        int status,
        String message,
        String path
) {
}