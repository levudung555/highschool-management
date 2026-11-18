package com.example.highschool_management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceStudentResponse {
    private Long maXL;
    private Long maHS;
    private String maSoHS;
    private String hoTen;
    private LocalDate ngaySinh;
    private String trangThai;   // CO_MAT, VANG_CO_PHEP, VANG_KHONG_PHEP, DI_MUON
    private String ghiChu;
}
