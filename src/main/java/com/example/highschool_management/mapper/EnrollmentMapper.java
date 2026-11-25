package com.example.highschool_management.mapper;

import com.example.highschool_management.dto.response.EnrollmentResponse;
import com.example.highschool_management.model.Enrollment;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentMapper {
  public EnrollmentResponse sangResponse(Enrollment e) {
    return new EnrollmentResponse(
        e.getMaXepLop(),
        e.getMaHocSinh(),
        e.getMaLop(),
        e.getTuNgay(),
        e.getDenNgay(),
        e.getLyDoKetThuc(),
        e.getMaTKNguoiThucHien(),
        e.getThoiDiemThucHien(),
        e.getMaXepLopNguon(),
        e.getLyDoChuyen(), e.getMaHoSoNhapHoc());
  }
}
