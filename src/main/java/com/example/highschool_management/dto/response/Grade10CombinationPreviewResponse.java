package com.example.highschool_management.dto.response;

import java.util.List;

public record Grade10CombinationPreviewResponse(
        Long maToHop,
        String kyHieu,
        List<Grade10ClassPreviewResponse> danhSachLop
) {}
