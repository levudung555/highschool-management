package com.example.highschool_management.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record LogoutRequest(
        @NotBlank(message = "Token is required") String token) {}
