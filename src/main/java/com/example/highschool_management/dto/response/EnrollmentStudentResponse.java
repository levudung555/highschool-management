package com.example.highschool_management.dto.response;

import java.time.LocalDate;

/** Dữ liệu chỉ đọc phục vụ nghiệp vụ UC08/UC09. */
public record EnrollmentStudentResponse(
    Long maHocSinh, String maSoHocSinh, String hoTen, LocalDate ngaySinh) {}
