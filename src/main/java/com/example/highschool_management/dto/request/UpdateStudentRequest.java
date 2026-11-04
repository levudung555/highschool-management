package com.example.highschool_management.dto.request;

import com.example.highschool_management.enums.SexEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
@Getter
@Setter
public class UpdateStudentRequest {
    @NotBlank
    String hoTen;
    @NotNull
    Date ngaySinh;
    @NotNull(message = "Giới tính không được để trống")
    SexEnum gioiTinh;
    String diaChi;
    String sdt;
    @Email
    String email;
}
