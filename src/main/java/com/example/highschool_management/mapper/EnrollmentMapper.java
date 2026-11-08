package com.example.highschool_management.mapper;
import com.example.highschool_management.dto.response.EnrollmentResponse;
import com.example.highschool_management.model.Enrollment;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentMapper {
    // Chỉ chuyển dữ liệu, không thay đổi người hoặc thời điểm thực hiện.
    public EnrollmentResponse sangResponse(Enrollment e) {
        return new EnrollmentResponse(
            e.getMaXepLop(), e.getMaHocSinh(), e.getMaLop(), e.getTuNgay(),
            e.getDenNgay(), e.getLyDoKetThuc(), e.getMaTaiKhoanThucHien(),
            e.getThoiDiemThucHien(), e.getHoTen(), e.getMaSoHocSinh(),
            e.getTenLop(), e.getNamHoc(), e.getTenNguoiThucHien());
    }
}
