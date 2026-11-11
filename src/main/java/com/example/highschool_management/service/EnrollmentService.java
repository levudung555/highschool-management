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
    EnrollmentResponse xepLop(@NotNull @Valid EnrollStudentRequest request);
    EnrollmentResponse chuyenLop(@NotNull @Valid TransferStudentRequest request);
    EnrollmentResponse chiTiet(Long maXepLop);
    List<EnrollmentResponse> lichSu(Long maHocSinh);
    PageResponse<ClassStudentResponse> danhSachLop(Long maLop, LocalDate ngay, int trang, int kichThuoc);
}
