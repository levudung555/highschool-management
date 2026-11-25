package com.example.highschool_management.dto.request;

import com.example.highschool_management.enums.SexEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
@Getter
@Setter
public class UpdateStudentRequest {
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
    @Email
    @Size(max = 254)
    String email;
}
