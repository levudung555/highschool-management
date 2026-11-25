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
    Long maTKNguoiThucHien,
    LocalDateTime thoiDiemThucHien,
    Long maXepLopNguon,
    String lyDoChuyen, Long maHoSoNhapHoc, String hoTen, String maSoHocSinh,
    String tenLop, String namHoc, String tenNguoiThucHien) {}
