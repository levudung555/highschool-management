package com.example.highschool_management.dto.auth.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponse {
  String token;
  boolean authenticated;

  public String getAccessToken() {
    return token;
  }
}
