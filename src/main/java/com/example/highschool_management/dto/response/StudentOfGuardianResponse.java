package com.example.highschool_management.dto.response;

import com.example.highschool_management.enums.SexEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentOfGuardianResponse {
    private Long maHS;
    private String maSoHS;
    private String hoTen;
    private LocalDate ngaySinh;
    private SexEnum gioiTinh;
    private String diaChi;
    private String sdt;
    private String email;
    private String moiQuanHe;
    private Boolean lienHeChinh;
}
