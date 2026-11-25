package com.example.highschool_management.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.List;

public record ConfirmGrade10PlacementRequest(
        @NotNull @Positive Long maNamHoc,
        @NotBlank @Pattern(regexp = "[0-9a-f]{64}") String maKiemTraPhuongAn,
        @NotNull LocalDate ngayVaoLop,
        List<@NotNull @Valid PlacementAdjustment> danhSachDieuChinh
) {
    public ConfirmGrade10PlacementRequest {
        if (danhSachDieuChinh == null) danhSachDieuChinh = List.of();
    }

    public record PlacementAdjustment(
            @NotNull @Positive Long maHoSoNhapHoc,
            @NotNull @Positive Long maLopDich
    ) {}
}
