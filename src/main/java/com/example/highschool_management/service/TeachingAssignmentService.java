package com.example.highschool_management.service;
//
import com.example.highschool_management.dto.response.ClassSubjectOptionResponse;

import com.example.highschool_management.dto.request.CreateTeachingAssignmentRequest;
import com.example.highschool_management.dto.response.TeachingAssignmentResponse;
import com.example.highschool_management.model.TeachingAssignment;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public interface TeachingAssignmentService {
    List<ClassSubjectOptionResponse> danhSachLuaChon();
    TeachingAssignmentResponse tao(@NotNull @Valid CreateTeachingAssignmentRequest phanCong);
    TeachingAssignmentResponse chiTiet(Long maPhanCong);
    List<TeachingAssignmentResponse> lichSuTheoLopMonHoc(Long maLopMonHoc);
}
