package com.example.highschool_management.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;


public class HomeroomAssignment {

    private Long maPhanCong;

    public Long getMaPhanCong() {
        return maPhanCong;
    }

    public void setMaPhanCong(Long maPhanCong) {
        this.maPhanCong = maPhanCong;
    }

    public Long getMaLop() {
        return maLop;
    }

    public void setMaLop(Long maLop) {
        this.maLop = maLop;
    }

    public Long getMaGiaoVien() {
        return maGiaoVien;
    }

    public void setMaGiaoVien(Long maGiaoVien) {
        this.maGiaoVien = maGiaoVien;
    }

    public LocalDate getTuNgay() {
        return tuNgay;
    }

    public void setTuNgay(LocalDate tuNgay) {
        this.tuNgay = tuNgay;
    }

    public LocalDate getDenNgay() {
        return denNgay;
    }

    public void setDenNgay(LocalDate denNgay) {
        this.denNgay = denNgay;
    }

    public String getTrangThaiDuyet() {
        return trangThaiDuyet;
    }

    public void setTrangThaiDuyet(String trangThaiDuyet) {
        this.trangThaiDuyet = trangThaiDuyet;
    }

    public Long getMaTaiKhoanDuyet() {
        return maTaiKhoanDuyet;
    }

    public void setMaTaiKhoanDuyet(Long maTaiKhoanDuyet) {
        this.maTaiKhoanDuyet = maTaiKhoanDuyet;
    }

    public LocalDateTime getDuyetLuc() {
        return duyetLuc;
    }

    public void setDuyetLuc(LocalDateTime duyetLuc) {
        this.duyetLuc = duyetLuc;
    }

    public String getTenLop() {
        return tenLop;
    }

    public void setTenLop(String tenLop) {
        this.tenLop = tenLop;
    }

    public String getTenGiaoVien() {
        return tenGiaoVien;
    }

    public void setTenGiaoVien(String tenGiaoVien) {
        this.tenGiaoVien = tenGiaoVien;
    }

    public String getNamHoc() {
        return namHoc;
    }

    public void setNamHoc(String namHoc) {
        this.namHoc = namHoc;
    }

    public String getTenNguoiDuyet() {
        return tenNguoiDuyet;
    }

    public void setTenNguoiDuyet(String tenNguoiDuyet) {
        this.tenNguoiDuyet = tenNguoiDuyet;
    }

    private Long maLop;
    private Long maGiaoVien;

    private LocalDate tuNgay;
    private LocalDate denNgay;

    private String trangThaiDuyet;
    private Long maTaiKhoanDuyet;
    private LocalDateTime duyetLuc;

    // Thông tin đọc thêm bằng JOIN để hiển thị.
    private String tenLop;
    private String tenGiaoVien;
    private String namHoc;
    private String tenNguoiDuyet;
}