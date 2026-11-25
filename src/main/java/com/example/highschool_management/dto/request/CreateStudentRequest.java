package com.example.highschool_management.dto.request;

import com.example.highschool_management.enums.SexEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

@Getter
@Setter
public class CreateStudentRequest {
    @NotBlank
    @Size(max = 150)
    String hoTen;
    @NotNull
    Date ngaySinh;
    @NotNull(message = "Giới tính không được để trống")
    SexEnum gioiTinh;
    @Size(max = 500)
    String diaChi;
    @Size(max = 20)
    String sdt;
    @NotBlank(message = "CCCD không được để trống")
    @Size(max = 100)
    String cmnd;

    @Valid
    StudentGuardianInputRequest nguoiGiamHo;
}
