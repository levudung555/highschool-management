package com.example.highschool_management.integration;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.highschool_management.dao.RoleDao;
import com.example.highschool_management.dao.UserDao;
import com.example.highschool_management.enums.RoleType;
import com.example.highschool_management.model.Role;
import com.example.highschool_management.model.User;
import com.example.highschool_management.service.JwtTokenService;
import com.jayway.jsonpath.JsonPath;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.context.annotation.Import;

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:auth;MODE=MySQL;DB_CLOSE_DELAY=-1",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "spring.datasource.username=sa",
      "spring.datasource.password=",
      "spring.sql.init.mode=always",
      "spring.sql.init.schema-locations=classpath:uc09-schema.sql",
      "app.seed.enabled=true"
    })
@AutoConfigureMockMvc
@Import(AuthIntegrationTest.TestSecurityController.class)
@Sql("/uc09-schema.sql")
class AuthIntegrationTest {

  @Autowired MockMvc mvc;
  @Autowired UserDao userDao;
  @Autowired RoleDao roleDao;
  @Autowired PasswordEncoder passwordEncoder;
  @Autowired JwtTokenService jwtTokenService;

  @RestController
  static class TestSecurityController {
    @GetMapping("/test/admin-only")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminOnly() {
      return "admin-content";
    }

    @GetMapping("/test/protected")
    public String protectedEndpoint() {
      return "protected-content";
    }
  }

  @BeforeEach
  void ensureSeedData() {
    Role adminRole =
        roleDao
            .findByName(RoleType.ADMIN.name())
            .orElseGet(
                () ->
                    roleDao.save(
                        Role.builder()
                            .name(RoleType.ADMIN.name())
                            .description("Quản trị viên")
                            .build()));
    roleDao
        .findByName(RoleType.TEACHER.name())
        .orElseGet(
            () ->
                roleDao.save(
                    Role.builder().name(RoleType.TEACHER.name()).description("Giáo viên").build()));
    roleDao
        .findByName(RoleType.STUDENT.name())
        .orElseGet(
            () ->
                roleDao.save(
                    Role.builder().name(RoleType.STUDENT.name()).description("Học sinh").build()));

    if (!userDao.existsByUsername("admin")) {
      userDao.save(
          User.builder()
              .username("admin")
              .fullName("Administrator")
              .email("admin@example.com")
              .password(passwordEncoder.encode("adminpassword"))
              .enabled(true)
              .roles(Set.of(adminRole))
              .build());
    }
  }

  @Test
  void adminLogin_ReturnsJwtToken() throws Exception {
    mvc.perform(
            post("/auth/token")
                .contentType("application/json")
                .content("{\"username\":\"admin\",\"password\":\"adminpassword\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(1000))
        .andExpect(jsonPath("$.result.authenticated").value(true))
        .andExpect(jsonPath("$.result.token").isNotEmpty());
  }

  @Test
  void loginWithWrongPassword_ReturnsAuthenticationFailed() throws Exception {
    mvc.perform(
            post("/auth/token")
                .contentType("application/json")
                .content("{\"username\":\"admin\",\"password\":\"wrongpassword\"}"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value(1004))
        .andExpect(jsonPath("$.message").value("Authentication Failed"));
  }

  @Test
  void callProtectedEndpoint_WithoutToken_Returns401Json() throws Exception {
    mvc.perform(get("/test/protected"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value(1004))
        .andExpect(jsonPath("$.message").value("Authentication Failed"));
  }

  @Test
  void regularUser_CallingAdminMethod_Returns403Json() throws Exception {
    Role studentRole = roleDao.findByName(RoleType.STUDENT.name()).orElseThrow();
    User studentUser =
        User.builder()
            .username("student1")
            .fullName("Student One")
            .email("student1@example.com")
            .password(passwordEncoder.encode("studentpassword"))
            .enabled(true)
            .roles(Set.of(studentRole))
            .build();
    userDao.save(studentUser);

    String studentToken = jwtTokenService.generateAuthToken(studentUser, 3600L);

    // Student calling ADMIN-only endpoint -> 403 Forbidden with code 1032
    mvc.perform(
            get("/test/admin-only").header("Authorization", "Bearer " + studentToken))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value(1032))
        .andExpect(jsonPath("$.message").value("You do not have permission"));

    // Admin calling ADMIN-only endpoint -> 200 OK
    var adminLoginRes =
        mvc.perform(
                post("/auth/token")
                    .contentType("application/json")
                    .content("{\"username\":\"admin\",\"password\":\"adminpassword\"}"))
            .andExpect(status().isOk())
            .andReturn();
    String adminToken =
        JsonPath.read(adminLoginRes.getResponse().getContentAsString(), "$.result.token");

    mvc.perform(
            get("/test/admin-only").header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk());
  }

  @Test
  void logout_InvalidatesToken_IntrospectReturnsFalse_AndTokenCannotBeUsedAgain()
      throws Exception {
    var adminLoginRes =
        mvc.perform(
                post("/auth/token")
                    .contentType("application/json")
                    .content("{\"username\":\"admin\",\"password\":\"adminpassword\"}"))
            .andExpect(status().isOk())
            .andReturn();
    String token =
        JsonPath.read(adminLoginRes.getResponse().getContentAsString(), "$.result.token");

    // Introspect before logout -> valid: true
    mvc.perform(
            post("/auth/introspect")
                .contentType("application/json")
                .content("{\"token\":\"" + token + "\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(1000))
        .andExpect(jsonPath("$.result.valid").value(true));

    // Logout
    mvc.perform(
            post("/auth/logout")
                .contentType("application/json")
                .content("{\"token\":\"" + token + "\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(1000));

    // Introspect after logout -> valid: false
    mvc.perform(
            post("/auth/introspect")
                .contentType("application/json")
                .content("{\"token\":\"" + token + "\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(1000))
        .andExpect(jsonPath("$.result.valid").value(false));

    // Token cannot be used again -> 401 Unauthorized (code 1004)
    mvc.perform(get("/test/protected").header("Authorization", "Bearer " + token))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value(1004));
  }

  @Test
  void refresh_WithExpiredTokenWithinRefreshDuration_SucceedsAndInvalidatesOldToken()
      throws Exception {
    User admin = userDao.findByUsername("admin").orElseThrow();

    // Generate token that expired 120s ago (within 7200s refresh window)
    String expiredToken = jwtTokenService.generateAuthToken(admin, -120L);

    // Refresh with expired token
    var refreshRes =
        mvc.perform(
                post("/auth/refresh")
                    .contentType("application/json")
                    .content("{\"token\":\"" + expiredToken + "\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(1000))
            .andExpect(jsonPath("$.result.authenticated").value(true))
            .andExpect(jsonPath("$.result.token").isNotEmpty())
            .andReturn();

    String newToken =
        JsonPath.read(refreshRes.getResponse().getContentAsString(), "$.result.token");
    assertNotEquals(expiredToken, newToken);

    // Old token is now revoked -> introspect is false
    mvc.perform(
            post("/auth/introspect")
                .contentType("application/json")
                .content("{\"token\":\"" + expiredToken + "\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result.valid").value(false));

    // New token works for protected endpoint
    mvc.perform(get("/test/protected").header("Authorization", "Bearer " + newToken))
        .andExpect(status().isOk());
  }

  @Test
  void introspect_InvalidOrMalformedToken_ReturnsValidFalse() throws Exception {
    mvc.perform(
            post("/auth/introspect")
                .contentType("application/json")
                .content("{\"token\":\"invalid.token.here\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(1000))
        .andExpect(jsonPath("$.result.valid").value(false));
  }
}
