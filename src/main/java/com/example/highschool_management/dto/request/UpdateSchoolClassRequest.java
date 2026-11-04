package com.example.highschool_management.dto.request;

import jakarta.validation.constraints.*;

public record UpdateSchoolClassRequest(
    @NotBlank(message = "Tên lớp học không được để trống")
        @Size(max = 30, message = "Tên lớp học không được vượt quá 30 ký tự")
        String tenLop,
    @NotBlank(message = "Trạng thái không được để trống")
        @Pattern(
            regexp = "HOAT_DONG|NGUNG_HOAT_DONG",
            message = "Trạng thái phải là 'HOAT_DONG' hoặc 'NGUNG_HOAT_DONG'")
        String trangThai) {}
