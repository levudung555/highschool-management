package com.example.highschool_management.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record ConfirmGrade10ClassesRequest(
        @NotNull @Positive Long maNamHoc,
        @NotEmpty List<@NotNull @Valid ClassItem> danhSachLop
) {
    public record ClassItem(
            @NotNull @Positive Long maToHop,
            @NotBlank String tenLop,
            @NotNull @Positive Integer siSoToiDa,
            @NotNull @Positive Integer thuTuXep
    ) {}
}