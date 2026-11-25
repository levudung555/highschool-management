package com.example.highschool_management.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest(
    properties = {
      "app.seed.enabled=false",
      "spring.datasource.url=jdbc:h2:mem:profiles;MODE=MySQL;DB_CLOSE_DELAY=-1",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "spring.datasource.username=sa",
      "spring.datasource.password=",
      "spring.sql.init.mode=never"
    })
@AutoConfigureMockMvc
@Sql({"/uc09-schema.sql", "/enrollment-fixture.sql"})
class ProfileIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired JdbcTemplate jdbc;

  @Test
  void studentCodeSkipsLegacyDemoAndUsesHighestNumericSuffix() throws Exception {
    jdbc.update("UPDATE hoc_sinh SET MaSoHS='HS0010' WHERE MaHS=1");
    jdbc.update("UPDATE hoc_sinh SET MaSoHS='HS0009' WHERE MaHS=2");
    jdbc.update("UPDATE hoc_sinh SET MaSoHS='DEMO-UC09-001' WHERE MaHS=3");
    mvc.perform(
            auth(post("/student")).contentType("application/json").content(student("New student")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.maSoHS").value("HS0011"));
    assertEquals(
        "DEMO-UC09-001",
        jdbc.queryForObject("SELECT MaSoHS FROM hoc_sinh WHERE MaHS=3", String.class));
  }

  @Test
  void studentCodeStartsAtOneWhenOnlyLegacyCodesExist() throws Exception {
    jdbc.update("UPDATE hoc_sinh SET MaSoHS=CONCAT('DEMO-', MaHS)");
    mvc.perform(
            auth(post("/student"))
                .contentType("application/json")
                .content(student("First student")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.maSoHS").value("HS0001"));
  }

  @Test
  void teacherCodeAlsoSkipsLegacyDemo() throws Exception {
    jdbc.update(
        "INSERT INTO giao_vien(MaSoGV,HoTen)"
            + " VALUES('GV0010','Existing'),('GV0009','Existing'),('GV-DEMO','Demo')");
    mvc.perform(
            auth(post("/teacher"))
                .contentType("application/json")
                .content(
                    "{\"hoTen\":\"New"
                        + " teacher\",\"ngaySinh\":\"1990-01-01\",\"sdt\":\"0999999999\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.maSoGV").value("GV0011"));
  }

  private String token(String user) throws Exception {
    var response =
        mvc.perform(
                post("/auth/token")
                    .contentType("application/json")
                    .content("{\"username\":\"" + user + "\",\"password\":\"Test@12345\"}"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return JsonPath.read(response, "$.result.token");
  }

  private MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder request)
      throws Exception {
    return request.header("Authorization", "Bearer " + token("giaovu"));
  }

  private String student(String name) {
    return "{\"hoTen\":\""
        + name
        + "\",\"ngaySinh\":\"2010-02-03\",\"gioiTinh\":\"NU\",\"sdt\":\"0901234567\",\"cmnd\":\"000000000010\",\"diemDauVao\":24.5}";
  }

  @Test
  void studentCreateUpdateSearchAndDeletePersist() throws Exception {
    var result =
        mvc.perform(auth(post("/student")).contentType("application/json").content(student("Demo")))
            .andExpect(status().isOk())
            .andReturn();
    Number id = JsonPath.read(result.getResponse().getContentAsString(), "$.maHS");
    mvc.perform(
            auth(put("/student/" + id)).contentType("application/json").content(student("Da sua")))
        .andExpect(status().isOk());
    assertEquals(
        "Da sua", jdbc.queryForObject("SELECT HoTen FROM hoc_sinh WHERE MaHS=?", String.class, id));
    mvc.perform(auth(get("/student/search").param("hoTen", "Da sua")))
        .andExpect(jsonPath("$[0].hoTen").value("Da sua"));
    mvc.perform(auth(delete("/student?maHS=" + id)))
        .andExpect(status().isOk())
        .andExpect(content().string("true"));
  }

  @Test
  void teacherStatusAndGuardianRelationshipPersist() throws Exception {
    String teacher =
        "{\"hoTen\":\"Giao vien"
            + " demo\",\"ngaySinh\":\"1990-01-01\",\"sdt\":\"0911111111\",\"chuyenMon\":\"Toan\",\"trangThai\":\"DANG_CONG_TAC\"}";
    var result =
        mvc.perform(auth(post("/teacher")).contentType("application/json").content(teacher))
            .andExpect(status().isOk())
            .andReturn();
    Number teacherId = JsonPath.read(result.getResponse().getContentAsString(), "$.maGV");
    mvc.perform(
            auth(put("/teacher/" + teacherId))
                .contentType("application/json")
                .content(teacher.replace("DANG_CONG_TAC", "NGUNG_CONG_TAC")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.trangThai").value("NGUNG_CONG_TAC"));
    var guardian =
        mvc.perform(
                auth(post("/guardian"))
                    .contentType("application/json")
                    .content("{\"hoTen\":\"Me demo\",\"sdt\":\"0922222222\"}"))
            .andExpect(status().isOk())
            .andReturn();
    Number guardianId = JsonPath.read(guardian.getResponse().getContentAsString(), "$.maNGH");
    mvc.perform(
            auth(post("/guardian/link"))
                .contentType("application/json")
                .content(
                    "{\"maHS\":1,\"maNGH\":"
                        + guardianId
                        + ",\"moiQuanHe\":\"Me\",\"lienHeChinh\":true}"))
        .andExpect(status().isOk());
    mvc.perform(auth(get("/guardian/student/1")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].moiQuanHe").value("Me"));
    mvc.perform(auth(delete("/guardian/unlink?maHS=1&maNGH=" + guardianId)))
        .andExpect(status().isOk());
  }

  @Test
  void filtersUseApprovedEffectiveEnrollmentsAndEndpointsEnforceRoles() throws Exception {
    mvc.perform(auth(get("/student"))).andExpect(status().isOk());
    jdbc.update(
        "INSERT INTO xep_lop(MaHS,MaLop,TuNgay,DenNgay,MaTKNguoiThucHien,ThoiDiemThucHien)"
            + " VALUES(1,1,DATE '2026-01-01',DATE '2027-05-31',1,CURRENT_TIMESTAMP)");
    mvc.perform(auth(get("/student/search?maLop=1&maKhoi=1&maNamHoc=1")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));
    mvc.perform(auth(get("/student/search?maLop=2"))).andExpect(jsonPath("$.length()").value(0));
    mvc.perform(get("/student")).andExpect(status().isUnauthorized());
    mvc.perform(delete("/teacher?maGV=1").header("Authorization", "Bearer " + token("gv")))
        .andExpect(status().isForbidden());
  }
}
