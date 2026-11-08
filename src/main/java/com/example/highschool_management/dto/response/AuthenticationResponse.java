package com.example.highschool_management.dto.response;

import lombok.Builder;

@Builder
public record AuthenticationResponse(String token, boolean authenticated) {}
