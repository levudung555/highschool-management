package com.example.highschool_management.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record EnrollStudentRequest(
    @NotNull @Positive Long maHocSinh, @NotNull @Positive Long maLop, @NotNull LocalDate tuNgay) {}
