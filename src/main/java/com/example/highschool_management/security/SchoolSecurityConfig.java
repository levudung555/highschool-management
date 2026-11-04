package com.example.highschool_management.security;

import com.example.highschool_management.dao.AuthDao;
import com.example.highschool_management.dto.response.ApiError;
import java.security.SecureRandom;
import java.time.*;
import java.util.*;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class SchoolSecurityConfig {
  public static final String ISSUER = "school-management";
  public static final String AUDIENCE = "school-web";
  public static final Set<String> ROLES = Set.of("ADMIN", "BGH", "GIAO_VU", "GIAO_VIEN");

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  SecretKey jwtKey(@Value("${JWT_SECRET_BASE64:}") String configured) {
    byte[] bytes;
    if (configured.isBlank()) {
      bytes = new byte[32];
      new SecureRandom().nextBytes(bytes);
    } else {
      try {
        bytes = Base64.getDecoder().decode(configured);
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("JWT_SECRET_BASE64 phải là Base64 hợp lệ");
      }
    }
    if (bytes.length < 32)
      throw new IllegalArgumentException("JWT_SECRET_BASE64 cần ít nhất 32 byte sau giải mã");
    return new SecretKeySpec(bytes, "HmacSHA256");
  }

  @Bean
  JwtEncoder jwtEncoder(SecretKey key) {
    return NimbusJwtEncoder.withSecretKey(key).build();
  }

  @Bean
  JwtDecoder jwtDecoder(SecretKey key, Clock clock, AuthDao dao) {
    var decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    var timestamp = new JwtTimestampValidator(Duration.ofSeconds(30));
    timestamp.setClock(clock);
    OAuth2TokenValidator<Jwt> claims =
        jwt -> {
          if (jwt.getExpiresAt() == null
              || jwt.getSubject() == null
              || jwt.getAudience() == null
              || !jwt.getAudience().contains(AUDIENCE)) return invalid();
          try {
            long id = Long.parseLong(jwt.getSubject());
            Number version = jwt.getClaim("ver");
            var user = dao.timTheoMa(id).orElse(null);
            if (user == null
                || !"HOAT_DONG".equals(user.trangThai())
                || version == null
                || version.longValue() != user.tokenVersion()) return invalid();
            return OAuth2TokenValidatorResult.success();
          } catch (NumberFormatException | ClassCastException e) {
            return invalid();
          }
        };
    decoder.setJwtValidator(
        new DelegatingOAuth2TokenValidator<>(timestamp, new JwtIssuerValidator(ISSUER), claims));
    return decoder;
  }

  private static OAuth2TokenValidatorResult invalid() {
    return OAuth2TokenValidatorResult.failure(
        new OAuth2Error("invalid_token", "Phiên đăng nhập không hợp lệ", null));
  }

  @Bean
  SecurityFilterChain schoolSecurity(HttpSecurity http, AuthDao dao, ObjectMapper json)
      throws Exception {
    var converter = new JwtAuthenticationConverter();
    // JWT sub is immutable MaTK. Roles are read from MySQL for every request, never trusted from
    // client.
    converter.setJwtGrantedAuthoritiesConverter(
        jwt ->
            dao.vaiTro(Long.parseLong(jwt.getSubject())).stream()
                .filter(ROLES::contains)
                .map(
                    role ->
                        (org.springframework.security.core.GrantedAuthority)
                            new SimpleGrantedAuthority("ROLE_" + role))
                .toList());
    http.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .csrf(c -> c.disable()) // Only Authorization: Bearer tokens; no authentication cookie.
        .authorizeHttpRequests(
            a ->
                a.requestMatchers("/api/v1/auth/login", "/api/v1/auth/options")
                    .permitAll()
                    .requestMatchers("/api/**")
                    .authenticated()
                    .anyRequest()
                    .permitAll())
        .oauth2ResourceServer(
            o ->
                o.jwt(j -> j.jwtAuthenticationConverter(converter))
                    .authenticationEntryPoint(
                        (req, res, e) -> {
                          res.setStatus(401);
                          res.setContentType("application/json;charset=UTF-8");
                          json.writeValue(
                              res.getWriter(),
                              new ApiError(
                                  401,
                                  "Phiên đăng nhập đã hết hạn hoặc không hợp lệ",
                                  req.getRequestURI()));
                        }))
        .exceptionHandling(
            e ->
                e.authenticationEntryPoint(
                        (req, res, x) -> {
                          res.setStatus(401);
                          res.setContentType("application/json;charset=UTF-8");
                          json.writeValue(
                              res.getWriter(),
                              new ApiError(401, "Cần đăng nhập", req.getRequestURI()));
                        })
                    .accessDeniedHandler(
                        (req, res, x) -> {
                          res.setStatus(403);
                          res.setContentType("application/json;charset=UTF-8");
                          json.writeValue(
                              res.getWriter(),
                              new ApiError(
                                  403, "Không có quyền thực hiện thao tác", req.getRequestURI()));
                        }));
    return http.build();
  }
}
