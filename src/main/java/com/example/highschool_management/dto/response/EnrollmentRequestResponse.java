package com.example.highschool_management.dto.response;

import java.time.LocalDate;

/** Dữ liệu chỉ đọc phục vụ nghiệp vụ UC08/UC09. */
public record EnrollmentRequestResponse(
    Long maXepLop,
    Long maHocSinh,
    String maSoHocSinh,
    String hoTen,
    Long maLop,
    String tenLop,
    String namHoc,
    LocalDate tuNgay,
    LocalDate denNgay,
    String trangThaiDuyet,
    Long maXepLopNguon,
    String lyDoChuyen) {}
