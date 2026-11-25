package com.example.highschool_management.service;
import com.example.highschool_management.dto.request.*;
import com.example.highschool_management.dto.response.*;
import com.example.highschool_management.model.Enrollment;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.core.env.Environment;

import java.time.LocalDate;
import java.util.List;

public interface EnrollmentService {
    Grade10PlacementStatusResponse trangThaiXepLop10(Long maNamHoc);
    Grade10PlacementPreviewResponse xemTruocXepLop10(Long maNamHoc);
    int chotXepLop10(ConfirmGrade10PlacementRequest yeuCau);
    EnrollmentResponse xepLop(@NotNull @Valid EnrollStudentRequest request);
    EnrollmentResponse chuyenLop(@NotNull @Valid TransferStudentRequest request);
    PageResponse<ClassStudentResponse> danhSachLop(Long maLop, LocalDate ngay, int trang, int kichThuoc);
    EnrollmentResponse chiTiet(Long maXepLop);
    List<EnrollmentResponse> lichSu(Long maHocSinh);
}
