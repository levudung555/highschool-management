package com.example.highschool_management.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateGuardianRequest {
    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 150)
    String hoTen;
    @Size(max = 20)
    String sdt;
    @Email(message = "Email không đúng định dạng")
    @Size(max = 254)
    String email;
    @Size(max = 500)
    String diaChi;
}
