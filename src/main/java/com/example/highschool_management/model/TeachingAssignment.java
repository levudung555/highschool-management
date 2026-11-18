package com.example.highschool_management.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class TeachingAssignment {

    private Long maPhanCong;
    private Long maLopMonHoc;
    private Long maGiaoVien;

    private LocalDate tuNgay;
    private LocalDate denNgay;

    private String trangThaiDuyet;
    private Long maTaiKhoanDuyet;
    private LocalDateTime duyetLuc;

    // Thông tin lấy thêm bằng JOIN.
    private String tenLop;
    private String tenMonHoc;
    private Integer soHocKy;
    private String namHoc;

    private String tenGiaoVien;
    private String tenNguoiDuyet;
}