package com.example.highschool_management.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentGuardianInputRequest {
    // Nếu chọn người giám hộ đã tồn tại thì truyền maNGH
    private Long maNGH;

    // Nếu tạo người giám hộ mới thì truyền các thông tin này
    @Size(max = 150, message = "Họ tên người giám hộ tối đa 150 ký tự")
    private String hoTen;

    @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
    private String sdt;

    @Email(message = "Email người giám hộ không đúng định dạng")
    private String email;

    @Size(max = 500, message = "Địa chỉ người giám hộ tối đa 500 ký tự")
    private String diaChi;

    // Quan hệ với học sinh: Bố, Mẹ, Ông, Bà, Người thân khác...
    private String moiQuanHe;

    // Là người liên hệ chính
    private Boolean lienHeChinh;
}
