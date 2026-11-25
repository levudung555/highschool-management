package com.example.highschool_management.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.highschool_management.dao.EnrollmentDao;
import java.time.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest(
    properties = {
      "app.seed.enabled=false",
      "spring.datasource.url=jdbc:h2:mem:uc09;MODE=MySQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=5000",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "spring.datasource.username=sa",
      "spring.datasource.password="
    })
@AutoConfigureMockMvc
@Sql({"/uc09-schema.sql", "/enrollment-fixture.sql"})
class EnrollmentIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired JdbcTemplate jdbc;
  @Autowired @MockitoSpyBean EnrollmentDao dao;

  @TestConfiguration
  static class Time {
    @Bean
    @Primary
    Clock fixedClock() {
      return Clock.fixed(Instant.parse("2026-11-04T03:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));
    }
  }

  private final java.util.Map<String, String> tokens =
      new java.util.concurrent.ConcurrentHashMap<>();

  private String token(String user) throws Exception {
    var result =
        mvc.perform(
                post("/auth/token")
                    .contentType("application/json")
                    .content("{\"username\":\"" + user + "\",\"password\":\"Test@12345\"}"))
            .andExpect(status().isOk())
            .andReturn();
    return com.jayway.jsonpath.JsonPath.read(
        result.getResponse().getContentAsString(), "$.result.token");
  }

  private MvcResult send(MockHttpServletRequestBuilder req, String user) throws Exception {
    if ("locked".equals(user))
      return mvc.perform(
              post("/auth/token")
                  .contentType("application/json")
                  .content("{\"username\":\"locked\",\"password\":\"Test@12345\"}"))
          .andReturn();
    if (user != null) {
      if (!tokens.containsKey(user)) tokens.put(user, token(user));
      req.header("Authorization", "Bearer " + tokens.get(user));
    }
    return mvc.perform(req).andReturn();
  }


  private MockHttpServletRequestBuilder enroll(int hs, int lop, String day) {
    return post("/api/v1/enrollments").contentType("application/json")
        .content("{\"maHocSinh\":%d,\"maLop\":%d,\"tuNgay\":\"%s\"}".formatted(hs,lop,day));
  }

  private long create(int hs, int lop, String day) throws Exception {
    MvcResult result = send(enroll(hs, lop, day), "giaovu");
    assertEquals(201, result.getResponse().getStatus(), result.getResponse().getContentAsString());
    Number id = com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.maXepLop");
    return id.longValue();
  }

  private MockHttpServletRequestBuilder transfer(long source, int target, String day) {
    return post("/api/v1/enrollments/transfers").contentType("application/json")
      .content("{\"maXepLopHienTai\":%d,\"maLopDich\":%d,\"ngayChuyen\":\"%s\",\"lyDo\":\"Chuyen theo nguyen vong\"}".formatted(source,target,day));
  }

  @Test void firstEnrollmentIsDirectAndRecordsAuthenticatedActor() throws Exception {
    long id = create(1,1,"2026-11-04");
    assertEquals(2L,dao.timTheoMa(id).orElseThrow().getMaTaiKhoanThucHien());
    assertNull(dao.timTheoMa(id).orElseThrow().getDenNgay());
    assertEquals(1,dao.demDanhSachLop(1L,LocalDate.of(2026,11,4)));
    assertEquals(0,dao.demDanhSachLop(1L,LocalDate.of(2026,11,3)));
  }

  @Test void duplicateInSameYearIsRejectedButNextYearIsAllowed() throws Exception {
    create(1,1,"2026-11-04");
    assertEquals(409,send(enroll(1,2,"2026-11-05"),"giaovu").getResponse().getStatus());
    create(1,4,"2027-09-01");
    assertEquals(0,dao.demDanhSachLop(1L,LocalDate.of(2027,9,1)));
  }

  @Test void futureTransferPreservesOldClassUntilPreviousDay() throws Exception {
    long source=create(1,1,"2026-11-04");
    assertEquals(201,send(transfer(source,2,"2026-11-15"),"giaovu").getResponse().getStatus());
    assertEquals(LocalDate.of(2026,11,14),dao.timTheoMa(source).orElseThrow().getDenNgay());
    assertEquals(1,dao.demDanhSachLop(1L,LocalDate.of(2026,11,14)));
    assertEquals(0,dao.demDanhSachLop(2L,LocalDate.of(2026,11,14)));
    assertEquals(0,dao.demDanhSachLop(1L,LocalDate.of(2026,11,15)));
    assertEquals(1,dao.demDanhSachLop(2L,LocalDate.of(2026,11,15)));
    assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM nhat_ky_he_thong",Integer.class));
    assertEquals("Chuyen theo nguyen vong",dao.timTheoMa(source).orElseThrow().getLyDoKetThuc());
  }

  @Test void nextDayTransferAllowsOneDayOldEnrollment() throws Exception {
    long source=create(1,1,"2026-11-04");
    assertEquals(201,send(transfer(source,2,"2026-11-05"),"giaovu").getResponse().getStatus());
    assertEquals(LocalDate.of(2026,11,4),dao.timTheoMa(source).orElseThrow().getDenNgay());
  }

  @Test void sameDaySameClassDifferentGradeDifferentYearAndClosedClassAreRejected() throws Exception {
    long source=create(1,1,"2026-11-04");
    for (int target : new int[]{1,3,4,5})
      assertEquals(409,send(transfer(source,target,"2026-11-05"),"giaovu").getResponse().getStatus());
    assertEquals(409,send(transfer(source,2,"2026-11-04"),"giaovu").getResponse().getStatus());
    assertNull(dao.timTheoMa(source).orElseThrow().getDenNgay());
  }

  @Test void invalidDatesMissingIdsAndBlankReasonAreRejected() throws Exception {
    assertEquals(400,send(enroll(1,1,"2026-11-03"),"giaovu").getResponse().getStatus());
    assertEquals(400,send(enroll(1,1,"2027-06-01"),"giaovu").getResponse().getStatus());
    assertEquals(404,send(enroll(99,1,"2026-11-04"),"giaovu").getResponse().getStatus());
    assertEquals(404,send(enroll(1,99,"2026-11-04"),"giaovu").getResponse().getStatus());
    assertEquals(400,send(post("/api/v1/enrollments/transfers").contentType("application/json")
      .content("{\"maXepLopHienTai\":1,\"maLopDich\":2,\"ngayChuyen\":\"2026-11-05\",\"lyDo\":\"  \"}"),"giaovu").getResponse().getStatus());
  }

  @Test void permissionsAreEnforcedAndApprovalIsGone() throws Exception {
    assertEquals(401,send(enroll(1,1,"2026-11-04"),null).getResponse().getStatus());
    assertEquals(403,send(enroll(1,1,"2026-11-04"),"gv").getResponse().getStatus());
    long source=create(1,1,"2026-11-04");
    assertEquals(404,send(put("/api/v1/enrollments/"+source+"/approval"),"bgh").getResponse().getStatus());
  }

  @Test void unassignedSearchExcludesFuturePlacementAndHistoryIncludesActorName() throws Exception {
    long source=create(1,1,"2026-11-15");
    assertEquals(2,dao.demHocSinh("",1L));
    assertEquals(3,dao.demHocSinh("",2L));
    MvcResult res=send(get("/api/v1/students/1/enrollments"),"giaovu");
    assertTrue(res.getResponse().getContentAsString().contains("tenNguoiThucHien"));
    assertEquals(200,send(get("/api/v1/enrollments?maNamHoc=1&tuKhoa=HS1"),"giaovu").getResponse().getStatus());
    assertEquals(404,send(get("/api/v1/enrollments/students?maNamHocChuaXep=999"),"giaovu").getResponse().getStatus());
  }

  @Test void failureAfterClosingSourceRollsBackEntireTransfer() throws Exception {
    long source=create(1,1,"2026-11-04");
    doThrow(new IllegalStateException("forced insert failure")).when(dao).tao(any());
    try {
      assertEquals(500, send(transfer(source,2,"2026-11-05"),"giaovu").getResponse().getStatus());
    } finally { reset(dao); }
    assertNull(dao.timTheoMa(source).orElseThrow().getDenNgay());
    assertEquals(1,dao.lichSu(1L).size());
  }

  @Test void overlappingOtherHistoryIsRejected() throws Exception {
    long source=create(1,1,"2026-11-04");
    jdbc.update("INSERT INTO xep_lop(MaHS,MaLop,TuNgay,MaTKNguoiThucHien,ThoiDiemThucHien) VALUES(1,2,'2026-12-01',1,CURRENT_TIMESTAMP)");
    assertEquals(409,send(transfer(source,2,"2026-11-05"),"giaovu").getResponse().getStatus());
    assertNull(dao.timTheoMa(source).orElseThrow().getDenNgay());
  }

  @Test void concurrentTransfersOnlyOneSucceeds() throws Exception {
    long source=create(1,1,"2026-11-04");
    ExecutorService pool=Executors.newFixedThreadPool(2);
    CountDownLatch start=new CountDownLatch(1);
    Callable<Integer> job=()->{ start.await(); return send(transfer(source,2,"2026-11-05"),"giaovu").getResponse().getStatus(); };
    try {
      Future<Integer> a=pool.submit(job), b=pool.submit(job); start.countDown();
      assertEquals(java.util.Set.of(201,409),java.util.Set.of(a.get(10,TimeUnit.SECONDS),b.get(10,TimeUnit.SECONDS)));
      assertEquals(2,dao.lichSu(1L).size());
    } finally {pool.shutdownNow();}
  }

  @Test void concurrentFirstPlacementsOnlyOneSucceeds() throws Exception {
    tokens.put("giaovu",token("giaovu"));
    ExecutorService pool=Executors.newFixedThreadPool(2);
    CountDownLatch start=new CountDownLatch(1);
    Callable<Integer> job=()->{ start.await(); return send(enroll(1,1,"2026-11-04"),"giaovu").getResponse().getStatus(); };
    try {
      Future<Integer> a=pool.submit(job), b=pool.submit(job); start.countDown();
      assertEquals(java.util.Set.of(201,409),java.util.Set.of(a.get(10,TimeUnit.SECONDS),b.get(10,TimeUnit.SECONDS)));
    } finally {pool.shutdownNow();}
  }

  @Test void rosterAndClassListUseSameInclusiveDate() throws Exception {
    long source=create(1,1,"2026-11-04");
    assertEquals(201,send(transfer(source,2,"2026-11-05"),"giaovu").getResponse().getStatus());
    MvcResult roster=send(get("/api/v1/classes/1/students?ngay=2026-11-04"),"giaovu");
    Number count=com.jayway.jsonpath.JsonPath.read(roster.getResponse().getContentAsString(),"$.tongSoPhanTu");
    assertEquals(1,count.intValue());
    MvcResult list=send(get("/api/v1/classes?maNamHoc=1"),"giaovu");
    assertEquals(200,list.getResponse().getStatus(),list.getResponse().getContentAsString());
  }

  @Test void failureWritingAuditRollsBackBothSourceAndTarget() throws Exception {
    long source=create(1,1,"2026-11-04");
    doThrow(new IllegalStateException("forced audit failure")).when(dao)
      .ghiNhatKyChuyenLop(anyLong(),anyLong(),anyLong(),any(),anyString(),any());
    try { assertEquals(500, send(transfer(source,2,"2026-11-05"),"giaovu").getResponse().getStatus()); }
    finally { reset(dao); }
    assertNull(dao.timTheoMa(source).orElseThrow().getDenNgay());
    assertEquals(1,dao.lichSu(1L).size());
  }
}
