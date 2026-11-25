package com.example.highschool_management.service;

import com.example.highschool_management.dto.request.CreateGuardianRequest;
import com.example.highschool_management.dto.request.LinkGuardianRequest;
import com.example.highschool_management.dto.request.UpdateGuardianRequest;
import com.example.highschool_management.dto.response.GuardianResponse;
import com.example.highschool_management.dto.response.StudentOfGuardianResponse;

import java.util.List;

public interface GuardianService {
    List<GuardianResponse> getAllGuardians();
    GuardianResponse getGuardianById(Long maNGH);
    GuardianResponse createGuardian(CreateGuardianRequest request);
    GuardianResponse updateGuardian(Long maNGH, UpdateGuardianRequest request);
    boolean deleteGuardian(List<Long> maNGH);
    List<GuardianResponse> getGuardiansByStudent(Long maHS);
    List<StudentOfGuardianResponse> getStudentsByGuardian(Long maNGH);
    boolean linkGuardian(LinkGuardianRequest request);
    boolean unlinkGuardian(Long maHS, Long maNGH);
}
