package com.example.highschool_management.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateGuardianRequest {
    @NotBlank(message = "Họ tên không được để trống")
    String hoTen;
    @NotBlank(message = "Số điện thoại không được để trống")
    String sdt;
    @Email(message = "Email không đúng định dạng")
    String email;
    String diaChi;
}
