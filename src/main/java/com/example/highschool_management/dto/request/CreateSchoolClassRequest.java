package com.example.highschool_management.dto.request;

import jakarta.validation.constraints.*;

public record CreateSchoolClassRequest(
    @NotNull(message = "Mã năm học không được để trống")
        @Positive(message = "Mã năm học phải là số dương")
        Long maNamHoc,
    @NotNull(message = "Mã khối không được để trống")
        @Positive(message = "Mã khối phải là số dương")
        Long maKhoi,
    @NotBlank(message = "Tên lớp không được để trống")
        @Size(max = 30, message = "Tên lớp không được vượt quá 30 ký tự")
        String tenLop) {}
