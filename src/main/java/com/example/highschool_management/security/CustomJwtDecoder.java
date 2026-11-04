package com.example.highschool_management.security;

import com.example.highschool_management.dao.AuthDao;
import com.example.highschool_management.service.JwtTokenService;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import javax.crypto.spec.SecretKeySpec;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomJwtDecoder implements JwtDecoder {

  @Value("${jwt.signer-key:${jwt.signerKey:1234567890123456789012345678901234567890123456789012345678901234}}")
  String signerKey;

  final JwtTokenService jwtTokenService;
  final AuthDao authDao;
  NimbusJwtDecoder nimbusJwtDecoder = null;

  @Override
  public Jwt decode(String token) throws JwtException {
    try {
      var signedJwt = jwtTokenService.verifierToken(token, false);
      var aud = signedJwt.getJWTClaimsSet().getAudience();
      if (aud != null && !aud.isEmpty() && !aud.contains(SchoolSecurityConfig.AUDIENCE)) {
        throw new BadJwtException("Audience token không hợp lệ");
      }
      String subject = signedJwt.getJWTClaimsSet().getSubject();
      var user = authDao.timTheoTen(subject).orElse(null);
      if (user == null) {
        try {
          user = authDao.timTheoMa(Long.parseLong(subject)).orElse(null);
        } catch (NumberFormatException ignored) {
        }
      }
      if (user != null && !"HOAT_DONG".equals(user.trangThai())) {
        throw new BadJwtException("Tài khoản không hợp lệ hoặc đã bị khóa");
      }
      Number ver = (Number) signedJwt.getJWTClaimsSet().getClaim("ver");
      if (ver != null && user != null && ver.longValue() != user.tokenVersion()) {
        throw new BadJwtException("Phiên đăng nhập đã bị thu hồi");
      }
    } catch (BadJwtException e) {
      throw e;
    } catch (Exception e) {
      throw new BadJwtException(e.getMessage(), e);
    }

    if (Objects.isNull(nimbusJwtDecoder)) {
      SecretKeySpec secretKeySpec =
          new SecretKeySpec(signerKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
      nimbusJwtDecoder =
          NimbusJwtDecoder.withSecretKey(secretKeySpec).macAlgorithm(MacAlgorithm.HS512).build();
    }
    return nimbusJwtDecoder.decode(token);
  }
}
