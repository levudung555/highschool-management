package com.example.highschool_management.model;

import java.math.BigDecimal;

public record Grade10PlacementCandidate(
        Long maHoSoNhapHoc,
        Long maHocSinh,
        String maSoHocSinh,
        String hoTen,
        Long maToHop,
        String kyHieu,
        BigDecimal tongDiemDauVao
) {}