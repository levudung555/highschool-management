package com.example.highschool_management.dto.response;

import java.time.LocalDate;

public record ClassStudentResponse(
    Long maXepLop,
    Long maHocSinh,
    String maSoHocSinh,
    String hoTen,
    LocalDate ngaySinh,
    LocalDate tuNgay,
    LocalDate denNgay) {}
