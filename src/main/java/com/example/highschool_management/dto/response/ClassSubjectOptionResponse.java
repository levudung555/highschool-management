package com.example.highschool_management.dto.response;

import java.time.LocalDate;

public record ClassSubjectOptionResponse(
        Long maLopMonHoc, Long maLop, String tenLop,
        Long maNamHoc, String namHoc, String tenMonHoc,
        Integer soHocKy, LocalDate batDau, LocalDate ketThuc,
        String trangThaiLop
) {}
