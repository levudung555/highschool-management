package com.example.highschool_management.service;

import com.example.highschool_management.dto.request.CreateGuardianRequest;
import com.example.highschool_management.dto.request.LinkGuardianRequest;
import com.example.highschool_management.dto.request.UpdateGuardianRequest;
import com.example.highschool_management.dto.response.GuardianResponse;

import java.util.List;

public interface GuardianService {
    GuardianResponse createGuardian(CreateGuardianRequest request);
    GuardianResponse updateGuardian(Long maNGH, UpdateGuardianRequest request);
    boolean deleteGuardian(List<Long> maNGH);
    GuardianResponse getGuardianById(Long maNGH);
    List<GuardianResponse> getAllGuardians();
    boolean linkGuardian(LinkGuardianRequest request);
    boolean unlinkGuardian(Long maHS, Long maNGH);
    List<GuardianResponse> getGuardiansByStudent(Long maHS);
}
