package com.example.highschool_management.dto.response;

import java.util.List;

public record Grade10PlacementPreviewResponse(
        Long maNamHoc,
        String maKiemTraPhuongAn,
        List<Grade10CombinationPreviewResponse> danhSachToHop
) {}