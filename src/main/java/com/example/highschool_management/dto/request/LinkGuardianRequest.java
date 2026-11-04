package com.example.highschool_management.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LinkGuardianRequest {
    @NotNull(message = "Mã học sinh không được để trống")
    Long maHS;
    @NotNull(message = "Mã người giám hộ không được để trống")
    Long maNGH;
    @NotBlank(message = "Mối quan hệ không được để trống")
    String moiQuanHe;
    Boolean lienHeChinh;
}
