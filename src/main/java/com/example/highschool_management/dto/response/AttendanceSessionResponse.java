package com.example.highschool_management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSessionResponse {
    private Long maPDD;
    private Long maLop;
    private String tenLop;
    private Long maHK;
    private Long maNH;
    private String tenNH;
    private Long maLMH;
    private String tenMonHoc;
    private LocalDate ngay;
    private String phamVi;
    private String maKhung;
    private String trangThai; // DANG_NHAP, DA_CHOT
    private boolean daKhoa;
    private Long maTKTao;
    private String nguoiTao;
    private List<AttendanceStudentResponse> danhSachHocSinh;
}
