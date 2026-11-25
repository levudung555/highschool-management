package com.example.highschool_management.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class HomeroomAssignmentResponse {

    private Long maPhanCong;

    private Long maLop;
    private String tenLop;
    private String namHoc;

    private Long maGiaoVien;
    private String tenGiaoVien;

    private LocalDate tuNgay;
    private LocalDate denNgay;

    private String trangThaiDuyet;
    private Long maTaiKhoanDuyet;
    private String tenNguoiDuyet;
    private LocalDateTime duyetLuc;
}