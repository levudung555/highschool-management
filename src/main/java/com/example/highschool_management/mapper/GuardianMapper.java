package com.example.highschool_management.mapper;

import com.example.highschool_management.dto.request.CreateGuardianRequest;
import com.example.highschool_management.dto.request.UpdateGuardianRequest;
import com.example.highschool_management.dto.response.GuardianResponse;
import com.example.highschool_management.model.Guardian;
import org.springframework.stereotype.Component;

@Component
public class GuardianMapper {

    public Guardian toEntity(CreateGuardianRequest request) {
        var guardian = new Guardian();
        guardian.setHoTen(request.getHoTen());
        guardian.setSdt(request.getSdt());
        guardian.setEmail(request.getEmail());
        guardian.setDiaChi(request.getDiaChi());
        return guardian;
    }

    public GuardianResponse toResponse(Guardian guardian) {
        var response = new GuardianResponse();
        response.setMaNGH(guardian.getMaNGH());
        response.setHoTen(guardian.getHoTen());
        response.setSdt(guardian.getSdt());
        response.setEmail(guardian.getEmail());
        response.setDiaChi(guardian.getDiaChi());
        return response;
    }

    public void updateEntity(UpdateGuardianRequest request, Guardian guardian) {
        guardian.setHoTen(request.getHoTen());
        guardian.setSdt(request.getSdt());
        guardian.setEmail(request.getEmail());
        guardian.setDiaChi(request.getDiaChi());
    }
}
