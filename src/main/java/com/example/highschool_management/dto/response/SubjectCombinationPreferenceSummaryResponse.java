package com.example.highschool_management.dto.response;

public record SubjectCombinationPreferenceSummaryResponse(
        Long maToHop,
        String kyHieu,
        String tenToHop,
        long soHocSinhDangKy
) {}