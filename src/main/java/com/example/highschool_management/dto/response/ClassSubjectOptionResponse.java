package com.example.highschool_management.dto.response;

import java.time.LocalDate;

public record ClassSubjectOptionResponse(
        Long maLMH,
        Long maLop,
        String tenLop,
        Long maNH,
        String maSoNH,
        String tenMon,
        Integer soHocKy,
        LocalDate ngayBatDau,
        LocalDate ngayKetThuc,
        String trangThai
) {
}
