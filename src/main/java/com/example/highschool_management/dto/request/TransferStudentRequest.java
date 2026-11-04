package com.example.highschool_management.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record TransferStudentRequest(
    @NotNull @Positive Long maHocSinh,
    @NotNull @Positive Long maLopDich,
    @NotNull LocalDate ngayChuyen,
    @NotBlank @Size(max = 255) String lyDo) {}
