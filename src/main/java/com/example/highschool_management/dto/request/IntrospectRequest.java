package com.example.highschool_management.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record IntrospectRequest(
        @NotBlank(message = "Token is required") String token) {}
