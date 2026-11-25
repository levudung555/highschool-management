package com.example.highschool_management.dto.response;

import com.example.highschool_management.model.Grade10PlacementCandidate;
import java.util.List;

public record Grade10ClassPreviewResponse(
        Long maLop,
        String tenLop,
        int siSoToiDa,
        List<Grade10PlacementCandidate> danhSachHocSinh
) {}
