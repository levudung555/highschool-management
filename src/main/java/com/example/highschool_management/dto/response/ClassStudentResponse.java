package com.example.highschool_management.dto.response;

import java.time.LocalDate;

public class ClassStudentResponse {

    private Long maXepLop;
    private Long maHocSinh;
    private String maSoHocSinh;
    private String hoTen;
    private LocalDate ngaySinh;
    private LocalDate tuNgay;
    private LocalDate denNgay;

    public ClassStudentResponse() {
    }

    public ClassStudentResponse(
            Long maXepLop,
            Long maHocSinh,
            String maSoHocSinh,
            String hoTen,
            LocalDate ngaySinh,
            LocalDate tuNgay,
            LocalDate denNgay) {

        this.maXepLop = maXepLop;
        this.maHocSinh = maHocSinh;
        this.maSoHocSinh = maSoHocSinh;
        this.hoTen = hoTen;
        this.ngaySinh = ngaySinh;
        this.tuNgay = tuNgay;
        this.denNgay = denNgay;
    }

    public Long getMaXepLop() {
        return maXepLop;
    }

    public void setMaXepLop(Long maXepLop) {
        this.maXepLop = maXepLop;
    }

    public Long getMaHocSinh() {
        return maHocSinh;
    }

    public void setMaHocSinh(Long maHocSinh) {
        this.maHocSinh = maHocSinh;
    }

    public String getMaSoHocSinh() {
        return maSoHocSinh;
    }

    public void setMaSoHocSinh(String maSoHocSinh) {
        this.maSoHocSinh = maSoHocSinh;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public LocalDate getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(LocalDate ngaySinh) {
        this.ngaySinh = ngaySinh;
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
}