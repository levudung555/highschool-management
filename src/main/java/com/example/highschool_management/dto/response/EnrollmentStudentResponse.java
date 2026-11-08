package com.example.highschool_management.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;


public class EnrollmentStudentResponse {
    private Long maHocSinh;
    private String maSoHocSinh;
    private String hoTen;
    private LocalDate ngaySinh;

    public EnrollmentStudentResponse() {}

    public EnrollmentStudentResponse(Long maHocSinh, String maSoHocSinh, String hoTen, LocalDate ngaySinh) {
        this.maHocSinh = maHocSinh;
        this.maSoHocSinh = maSoHocSinh;
        this.hoTen = hoTen;
        this.ngaySinh = ngaySinh;
    }

    public Long getMaHocSinh() { return maHocSinh; }
    public void setMaHocSinh(Long maHocSinh) { this.maHocSinh = maHocSinh; }

    public String getMaSoHocSinh() { return maSoHocSinh; }
    public void setMaSoHocSinh(String maSoHocSinh) { this.maSoHocSinh = maSoHocSinh; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public LocalDate getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(LocalDate ngaySinh) { this.ngaySinh = ngaySinh; }
}
