package com.example.highschool_management.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.highschool_management.exception.AccessException;
import com.example.highschool_management.security.SchoolSecurityConfig;
import com.example.highschool_management.service.SchoolClassService;
import java.time.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:auth;MODE=MySQL;DB_CLOSE_DELAY=-1",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "spring.datasource.username=sa",
      "spring.datasource.password=",
      "spring.sql.init.mode=never"
    })
@AutoConfigureMockMvc
@Sql("/uc09-schema.sql")
class AuthIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired JdbcTemplate jdbc;
  @Autowired JwtEncoder encoder;
  @Autowired Clock clock;
  @Autowired SchoolClassService classes;

  private String login(String username) throws Exception {
    var result =
        mvc.perform(
                post("/api/v1/auth/login")
                    .contentType("application/json")
                    .content("{\"tenDangNhap\":\"" + username + "\",\"matKhau\":\"Test@12345\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.nguoiDung.matKhauBam").doesNotExist())
            .andReturn();
    return com.jayway.jsonpath.JsonPath.read(
        result.getResponse().getContentAsString(), "$.accessToken");
  }

  @Test
  void loginAndMeExposeProfileWithoutPassword() throws Exception {
    String token = login("giaovu");
    mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.maTaiKhoan").value(2))
        .andExpect(jsonPath("$.vaiTro[0]").value("GIAO_VU"))
        .andExpect(jsonPath("$.matKhauBam").doesNotExist())
        .andExpect(header().string("Cache-Control", "no-store"));
  }

  @Test
  void badPasswordUnknownAndLockedHaveSameGenericResponse() throws Exception {
    for (String user : List.of("missing", "locked", "bgh")) {
      String password = user.equals("bgh") ? "wrong" : "Test@12345";
      mvc.perform(
              post("/api/v1/auth/login")
                  .contentType("application/json")
                  .content("{\"tenDangNhap\":\"" + user + "\",\"matKhau\":\"" + password + "\"}"))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.message").value("Tên đăng nhập hoặc mật khẩu không đúng"));
    }
  }

  @Test
  void invalidInputAndAnonymousAreRejected() throws Exception {
    mvc.perform(post("/api/v1/auth/login").contentType("application/json").content("{} "))
        .andExpect(status().isBadRequest());
    mvc.perform(get("/api/v1/classes/1")).andExpect(status().isUnauthorized());
    assertThrows(AccessException.class, () -> classes.getSchoolClassById(1L));
  }

  @Test
  void logoutRevokesToken() throws Exception {
    String token = login("bgh");
    mvc.perform(post("/api/v1/auth/logout").header("Authorization", "Bearer " + token))
        .andExpect(status().isNoContent());
    mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + token))
        .andExpect(status().isUnauthorized());
    mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + login("bgh")))
        .andExpect(status().isOk());
  }

  @Test
  void arenaEduTokenEndpointReturnsStandardResponse() throws Exception {
    mvc.perform(
            post("/auth/token")
                .contentType("application/json")
                .content("{\"username\":\"bgh\",\"password\":\"Test@12345\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(1000))
        .andExpect(jsonPath("$.result.token").isNotEmpty())
        .andExpect(jsonPath("$.result.authenticated").value(true));
  }

  @Test
  void lockingAccountAndChangingRolesTakeEffectImmediately() throws Exception {
    String token = login("bgh");
    jdbc.update("DELETE FROM tai_khoan_vai_tro WHERE MaTK=1");
    mvc.perform(put("/api/v1/enrollments/1/approval").header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());
    jdbc.update("UPDATE tai_khoan SET TrangThai='BI_KHOA' WHERE MaTK=1");
    mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + token))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void expiredWrongAudienceAndMalformedTokensAreRejected() throws Exception {
    for (boolean expired : List.of(true, false)) {
      var claims =
          JwtClaimsSet.builder()
              .issuer(SchoolSecurityConfig.ISSUER)
              .subject("1")
              .audience(List.of(expired ? SchoolSecurityConfig.AUDIENCE : "other-app"))
              .issuedAt(clock.instant().minusSeconds(3600))
              .expiresAt(clock.instant().plusSeconds(expired ? -120 : 600))
              .claim("ver", 0)
              .build();
      String token =
          encoder
              .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS512).build(), claims))
              .getTokenValue();
      mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + token))
          .andExpect(status().isUnauthorized());
    }
    mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer broken.jwt.token"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void introspectValidAndInvalidToken() throws Exception {
    String token = login("bgh");
    mvc.perform(
            post("/api/v1/auth/introspect")
                .contentType("application/json")
                .content("{\"token\":\"" + token + "\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.valid").value(true));

    mvc.perform(
            post("/api/v1/auth/introspect")
                .contentType("application/json")
                .content("{\"token\":\"broken.jwt.token\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.valid").value(false));

    mvc.perform(post("/api/v1/auth/logout").header("Authorization", "Bearer " + token))
        .andExpect(status().isNoContent());

    mvc.perform(
            post("/api/v1/auth/introspect")
                .contentType("application/json")
                .content("{\"token\":\"" + token + "\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.valid").value(false));
  }

  @Test
  void refreshTokenSuccessAndRevokesOldToken() throws Exception {
    String token1 = login("bgh");
    var refreshResult =
        mvc.perform(
                post("/api/v1/auth/refresh")
                    .contentType("application/json")
                    .content("{\"token\":\"" + token1 + "\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andReturn();

    String token2 =
        com.jayway.jsonpath.JsonPath.read(
            refreshResult.getResponse().getContentAsString(), "$.accessToken");
    assertNotEquals(token1, token2);

    mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + token2))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.maTaiKhoan").value(1));

    mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + token1))
        .andExpect(status().isUnauthorized());
  }
}
