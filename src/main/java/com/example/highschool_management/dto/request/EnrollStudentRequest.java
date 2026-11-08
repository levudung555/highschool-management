package com.example.highschool_management.dto.request;

import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.validation.constraints.*;

/** Dữ liệu client gửi lên; người thực hiện do server xác định. */
public class EnrollStudentRequest {
    @NotNull @Positive
    private Long maHocSinh;
    @NotNull @Positive
    private Long maLop;
    @NotNull
    private LocalDate tuNgay;

    public EnrollStudentRequest() {}

    public Long getMaHocSinh() { return maHocSinh; }
    public void setMaHocSinh(Long maHocSinh) { this.maHocSinh = maHocSinh; }

    public Long getMaLop() { return maLop; }
    public void setMaLop(Long maLop) { this.maLop = maLop; }

    public LocalDate getTuNgay() { return tuNgay; }
    public void setTuNgay(LocalDate tuNgay) { this.tuNgay = tuNgay; }
}
