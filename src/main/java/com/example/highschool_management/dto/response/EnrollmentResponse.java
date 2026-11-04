package com.example.highschool_management.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record EnrollmentResponse(
    Long maXepLop,
    Long maHocSinh,
    Long maLop,
    LocalDate tuNgay,
    LocalDate denNgay,
    String lyDoKetThuc,
    String trangThaiDuyet,
    Long maTaiKhoanDuyet,
    LocalDateTime duyetLuc,
    Long maXepLopNguon,
    String lyDoChuyen) {}
