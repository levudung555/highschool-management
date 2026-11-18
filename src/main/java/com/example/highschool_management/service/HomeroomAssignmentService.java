package com.example.highschool_management.service;

import com.example.highschool_management.dto.request.CreateHomeroomAssignmentRequest;
import com.example.highschool_management.dto.response.EnrollmentResponse;
import com.example.highschool_management.dto.response.HomeroomAssignmentResponse;
import com.example.highschool_management.model.HomeroomAssignment;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public interface HomeroomAssignmentService {
    HomeroomAssignmentResponse taoPhanCongCN(@NotNull @Valid CreateHomeroomAssignmentRequest request);
    HomeroomAssignmentResponse chiTiet(Long maPhanCong);
    List<HomeroomAssignmentResponse> lichSu(Long maLop);
}
