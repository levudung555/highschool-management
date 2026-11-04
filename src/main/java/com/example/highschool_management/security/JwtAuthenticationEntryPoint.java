package com.example.highschool_management.security;

import com.example.highschool_management.dto.response.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import tools.jackson.databind.ObjectMapper;

public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");

    ApiError apiError =
        new ApiError(
            HttpServletResponse.SC_UNAUTHORIZED,
            "Phiên đăng nhập đã hết hạn hoặc không hợp lệ",
            request.getRequestURI());

    response.getWriter().write(objectMapper.writeValueAsString(apiError));
    response.flushBuffer();
  }
}
