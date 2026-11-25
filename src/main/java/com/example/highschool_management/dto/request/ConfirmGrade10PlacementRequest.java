package com.example.highschool_management.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record ConfirmGrade10PlacementRequest(
        @NotNull @Positive Long maNamHoc,
        @NotBlank @Pattern(regexp = "[0-9a-f]{64}") String maKiemTraPhuongAn,
        @NotNull LocalDate ngayVaoLop
) {}
