package com.example.highschool_management.dto.request;

import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.validation.constraints.*;

/** Dữ liệu client gửi lên; người thực hiện do server xác định. */
public class TransferStudentRequest {
    @NotNull @Positive
    private Long maXepLopHienTai;
    @NotNull @Positive
    private Long maLopDich;
    @NotNull
    private LocalDate ngayChuyen;
    @NotBlank @Size(max = 255)
    private String lyDo;

    public TransferStudentRequest() {}

    public Long getMaXepLopHienTai() { return maXepLopHienTai; }
    public void setMaXepLopHienTai(Long maXepLopHienTai) { this.maXepLopHienTai = maXepLopHienTai; }

    public Long getMaLopDich() { return maLopDich; }
    public void setMaLopDich(Long maLopDich) { this.maLopDich = maLopDich; }

    public LocalDate getNgayChuyen() { return ngayChuyen; }
    public void setNgayChuyen(LocalDate ngayChuyen) { this.ngayChuyen = ngayChuyen; }

    public String getLyDo() { return lyDo; }
    public void setLyDo(String lyDo) { this.lyDo = lyDo; }
}
