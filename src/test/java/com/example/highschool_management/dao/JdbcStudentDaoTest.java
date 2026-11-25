package com.example.highschool_management.dao;

import static org.junit.jupiter.api.Assertions.*;

import com.example.highschool_management.enums.SexEnum;
import com.example.highschool_management.model.Student;
import java.math.BigDecimal;
import java.sql.Date;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:student_insert;MODE=MySQL",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.sql.init.schema-locations=classpath:uc09-schema.sql",
    "app.seed.enabled=false"
})
class JdbcStudentDaoTest {
  @Test
  void savesRequiredImportColumns(@Autowired StudentDao dao, @Autowired JdbcTemplate jdbc) {
    var student = new Student();
    student.setMaSoHS("HS0002");
    student.setHoTen("Test");
    student.setNgaySinh(Date.valueOf("2010-01-01"));
    student.setNgayTiepNhan(Date.valueOf("2026-09-01"));
    student.setGioiTinh(SexEnum.NU);
    student.setCmnd("000000000002");
    student.setDiemDauVao(new BigDecimal("24.75"));

    dao.insertStudent(student);

    assertTrue(dao.existsCCCD(student.getCmnd()));
    assertEquals(student.getDiemDauVao(), jdbc.queryForObject(
        "SELECT DiemDauVao FROM hoc_sinh WHERE MaHS=? AND NgayCapNhat IS NOT NULL",
        BigDecimal.class, student.getMaHS()));
  }
}
