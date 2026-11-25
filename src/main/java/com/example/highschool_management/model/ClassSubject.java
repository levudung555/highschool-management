package com.example.highschool_management.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ClassSubject {

    private Long maLopMonHoc;

    private Long maLop;
    private Long maMonHoc;
    private Long maHocKy;

    // Thông tin lấy từ lớp.
    private Long maNamHocCuaLop;
    private String trangThaiLop;

    // Thông tin lấy từ học kỳ.
    private Long maNamHocCuaHocKy;
    private LocalDate ngayBatDauHocKy;
    private LocalDate ngayKetThucHocKy;
}