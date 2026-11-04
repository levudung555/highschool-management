package com.example.highschool_management.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.highschool_management.entity.Role;
import com.example.highschool_management.entity.User;
import com.example.highschool_management.repository.InvalidatedTokenRepository;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class JwtTokenServiceTest {

  private JwtTokenService jwtTokenService;
  private InvalidatedTokenRepository invalidatedTokenRepository;
  private final String signerKey =
      "1234567890123456789012345678901234567890123456789012345678901234";

  @BeforeEach
  void setUp() {
    invalidatedTokenRepository = mock(InvalidatedTokenRepository.class);
    jwtTokenService = new JwtTokenService(invalidatedTokenRepository);
    jwtTokenService.SIGNER_KEY = signerKey;
    jwtTokenService.ISSUER = "school-management";
    jwtTokenService.VALID_DURATION = 3600L;
    jwtTokenService.REFRESHABLE_DURATION = 7200L;
  }

  @Test
  void testGenerateAndVerifyToken() throws Exception {
    User user =
        User.builder()
            .username("admin")
            .password("hash")
            .roles(Set.of(new Role("ADMIN"), new Role("BGH")))
            .build();
    String token = jwtTokenService.generateAuthToken(user, 3600L);

    assertNotNull(token);
    var signedJwt = jwtTokenService.verifierToken(token, false);
    assertEquals("admin", signedJwt.getJWTClaimsSet().getSubject());
    assertEquals("school-management", signedJwt.getJWTClaimsSet().getIssuer());
    assertTrue(signedJwt.getJWTClaimsSet().getStringClaim("scope").contains("ROLE_ADMIN"));
    assertTrue(signedJwt.getJWTClaimsSet().getStringClaim("scope").contains("ROLE_BGH"));
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
    verify(invalidatedTokenRepository).save(any());
  }

  @Test
  void printBcryptHash() {
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    String hash = encoder.encode("Test@12345");
    System.out.println("BCRYPT_HASH=" + hash);
    assertTrue(encoder.matches("Test@12345", hash));
  }
}
