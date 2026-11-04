package com.example.highschool_management.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

@Getter
@Setter
public class UpdateTeacherRequest {
    @NotBlank(message = "Họ tên không được để trống")
    String hoTen;
    @NotNull(message = "Ngày sinh không được để trống")
    Date ngaySinh;
    @NotBlank(message = "Số điện thoại không được để trống")
    String sdt;
    @Email(message = "Email không đúng định dạng")
    String email;
    String chuyenMon;
    String trangThai;
}
