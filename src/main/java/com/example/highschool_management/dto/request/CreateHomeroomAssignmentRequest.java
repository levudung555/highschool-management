package com.example.highschool_management.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class CreateHomeroomAssignmentRequest {

    @NotNull(message = "Mã lớp không được để trống")
    @Positive(message = "Mã lớp phải lớn hơn 0")
    private Long maLop;

    @NotNull(message = "Mã giáo viên không được để trống")
    @Positive(message = "Mã giáo viên phải lớn hơn 0")
    private Long maGiaoVien;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate tuNgay;

    private LocalDate denNgay;
}