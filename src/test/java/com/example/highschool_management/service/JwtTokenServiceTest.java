package com.example.highschool_management.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.highschool_management.dao.InvalidatedTokenDao;
import com.example.highschool_management.model.Role;
import com.example.highschool_management.model.User;
import com.example.highschool_management.service.impl.JwtTokenServiceImpl;
import java.lang.reflect.Field;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class JwtTokenServiceTest {

  private JwtTokenServiceImpl jwtTokenService;
  private InvalidatedTokenDao invalidatedTokenDao;
  private final String signerKey =
      "1234567890123456789012345678901234567890123456789012345678901234";

  @BeforeEach
  void setUp() throws Exception {
    invalidatedTokenDao = mock(InvalidatedTokenDao.class);
    jwtTokenService = new JwtTokenServiceImpl(invalidatedTokenDao);

    setField(jwtTokenService, "signerKey", signerKey);
    setField(jwtTokenService, "issuer", "highschool-management");
    setField(jwtTokenService, "validDuration", 3600L);
    setField(jwtTokenService, "refreshableDuration", 7200L);
  }

  private void setField(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }

  @Test
  void testGenerateAndVerifyToken() throws Exception {
    User user =
        User.builder()
            .username("admin")
            .password("hash")
            .roles(Set.of(new Role("ADMIN"), new Role("TEACHER")))
            .build();
    String token = jwtTokenService.generateAuthToken(user, 3600L);

    assertNotNull(token);
    var signedJwt = jwtTokenService.verifierToken(token, false);
    assertEquals("admin", signedJwt.getJWTClaimsSet().getSubject());
    assertEquals("highschool-management", signedJwt.getJWTClaimsSet().getIssuer());
    assertTrue(signedJwt.getJWTClaimsSet().getStringClaim("scope").contains("ROLE_ADMIN"));
    assertTrue(signedJwt.getJWTClaimsSet().getStringClaim("scope").contains("ROLE_TEACHER"));
  }

  @Test
  void testRevokeToken() throws Exception {
    User user =
        User.builder()
            .username("admin")
            .password("hash")
            .roles(Set.of(new Role("ADMIN")))
            .build();
    String token = jwtTokenService.generateAuthToken(user, 3600L);

    jwtTokenService.revokeToken(token);
    verify(invalidatedTokenDao).save(any());
  }

  @Test
  void printBcryptHash() {
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    String hash = encoder.encode("Test@12345");
    assertTrue(encoder.matches("Test@12345", hash));
  }
}
