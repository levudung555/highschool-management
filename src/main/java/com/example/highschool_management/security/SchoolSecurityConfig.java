package com.example.highschool_management.security;

import com.example.highschool_management.dto.response.ApiError;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SchoolSecurityConfig {
  public static final String ISSUER = "school-management";
  public static final String AUDIENCE = "school-web";
  public static final Set<String> ROLES = Set.of("ADMIN", "BGH", "GIAO_VU", "GIAO_VIEN");

  private static final String[] PUBLIC_POST_URLS = {
    "/api/v1/auth/login",
    "/api/v1/auth/token",
    "/api/v1/auth/introspect",
    "/api/v1/auth/logout",
    "/api/v1/auth/refresh",
    "/auth/token",
    "/auth/introspect",
    "/auth/logout",
    "/auth/refresh"
  };

  private static final String[] PUBLIC_GET_URLS = {
    "/api/v1/auth/options",
    "/",
    "/index.html",
    "/login.html",
    "/*.html",
    "/*.css",
    "/*.js",
    "/*.ico",
    "/css/**",
    "/js/**",
    "/vendor/**",
    "/error"
  };

  private final CustomJwtDecoder customJwtDecoder;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();

    httpSecurity
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .csrf(csrf -> csrf.disable())
        .cors(Customizer.withDefaults())
        .authorizeHttpRequests(
            request ->
                request
                    .requestMatchers(HttpMethod.POST, PUBLIC_POST_URLS)
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, PUBLIC_GET_URLS)
                    .permitAll()
                    .requestMatchers("/api/**", "/student/**", "/teacher/**", "/guardian/**")
                    .authenticated()
                    .anyRequest()
                    .permitAll())
        .oauth2ResourceServer(
            oauth2 ->
                oauth2
                    .jwt(
                        jwt ->
                            jwt.decoder(customJwtDecoder)
                                .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                    .authenticationEntryPoint(new JwtAuthenticationEntryPoint()))
        .exceptionHandling(
            exceptions ->
                exceptions
                    .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                    .accessDeniedHandler(
                        (req, res, accessDeniedException) -> {
                          res.setStatus(403);
                          res.setContentType("application/json;charset=UTF-8");
                          objectMapper.writeValue(
                              res.getWriter(),
                              new ApiError(
                                  403, "Không có quyền thực hiện thao tác", req.getRequestURI()));
                        }));

    return httpSecurity.build();
  }

  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter =
        new JwtGrantedAuthoritiesConverter();
    jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");
    JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
    return jwtAuthenticationConverter;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public JwtEncoder jwtEncoder(
      @Value(
              "${jwt.signerKey:${jwt.signer-key:1234567890123456789012345678901234567890123456789012345678901234}}")
          String signerKey) {
    JWK jwk =
        new OctetSequenceKey.Builder(signerKey.getBytes(StandardCharsets.UTF_8))
            .algorithm(JWSAlgorithm.HS512)
            .build();
    JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(jwk));
    return new NimbusJwtEncoder(jwkSource);
  }
}
