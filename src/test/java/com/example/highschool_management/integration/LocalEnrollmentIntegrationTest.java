package com.example.highschool_management.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:uc09local;MODE=MySQL;DB_CLOSE_DELAY=-1",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "spring.datasource.username=sa",
      "spring.datasource.password=",
      "spring.sql.init.mode=never"
    })
@ActiveProfiles("local")
@AutoConfigureMockMvc
@Sql("/uc09-schema.sql")
class LocalEnrollmentIntegrationTest {
  @org.springframework.boot.test.context.TestConfiguration
  static class Time {
    @org.springframework.context.annotation.Bean
    @org.springframework.context.annotation.Primary
    java.time.Clock localClock() {
      return java.time.Clock.fixed(
          java.time.Instant.parse("2026-11-04T03:00:00Z"), java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
    }
  }

  @Autowired MockMvc mvc;
  private String token;

  private org.springframework.test.web.servlet.ResultActions auth(
      org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request)
      throws Exception {
    if (token == null) {
      var login =
          mvc.perform(
                  post("/api/v1/auth/login")
                      .contentType("application/json")
                      .content("{\"tenDangNhap\":\"bgh\",\"matKhau\":\"Test@12345\"}"))
              .andExpect(status().isOk())
              .andReturn();
      token =
          com.jayway.jsonpath.JsonPath.read(
              login.getResponse().getContentAsString(), "$.accessToken");
    }
    return mvc.perform(request.header("Authorization", "Bearer " + token));
  }

  @Test
  void localProfileAlsoRequiresLoginAndUsesAuthenticatedAccount() throws Exception {
    mvc.perform(get("/api/v1/classes")).andExpect(status().isUnauthorized());
    var start = "2026-11-04";
    var response =
        auth(post("/api/v1/enrollments")
                .contentType("application/json")
                .content("{\"maHocSinh\":1,\"maLop\":1,\"tuNgay\":\"" + start + "\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.maTaiKhoanThucHien").value(1))
            .andExpect(jsonPath("$.trangThaiDuyet").doesNotExist())
            .andReturn().getResponse();
    auth(get(response.getHeader("Location"))).andExpect(status().isOk());
  }

  @Test
  void useCaseEndpointsReadCatalogClassesStudentsAndRequests() throws Exception {
    auth(get("/api/v1/classes/catalog"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.khoi[0].soKhoi").value(10));
    auth(get("/api/v1/classes?maNamHoc=1&maKhoi=1&trangThai=HOAT_DONG"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tongSoPhanTu").value(2))
        .andExpect(jsonPath("$.danhSach[0].namHoc").value("2026-2027"));
    auth(get("/api/v1/enrollments/students?tuKhoa=HS1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tongSoPhanTu").value(1));
    auth(post("/api/v1/enrollments")
            .contentType("application/json")
            .content("{\"maHocSinh\":1,\"maLop\":1,\"tuNgay\":\"2026-11-04\"}"))
        .andExpect(status().isCreated());
    auth(get("/api/v1/enrollments?maNamHoc=1&tuKhoa=HS1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tongSoPhanTu").value(1))
        .andExpect(jsonPath("$.danhSach[0].hoTen").value("Hoc sinh 1"));
    auth(get("/api/v1/classes?maKhoi=-1")).andExpect(status().isBadRequest());
    auth(get("/api/v1/enrollments?maNamHoc=-1")).andExpect(status().isBadRequest());
  }
}
