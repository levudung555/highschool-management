package com.example.highschool_management.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GuardianResponse {
    Long maNGH;
    String hoTen;
    String cmnd;
    String sdt;
    String diaChi;
    String moiQuanHe;
    Boolean lienHeChinh;
}
