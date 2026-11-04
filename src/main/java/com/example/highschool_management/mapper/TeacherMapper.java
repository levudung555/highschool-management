package com.example.highschool_management.mapper;

import com.example.highschool_management.dto.request.CreateTeacherRequest;
import com.example.highschool_management.dto.request.UpdateTeacherRequest;
import com.example.highschool_management.dto.response.TeacherResponse;
import com.example.highschool_management.model.Teacher;
import org.springframework.stereotype.Component;

@Component
public class TeacherMapper {

    public Teacher toEntity(CreateTeacherRequest request) {
        var teacher = new Teacher();
        teacher.setHoTen(request.getHoTen());
        teacher.setNgaySinh(request.getNgaySinh());
        teacher.setSdt(request.getSdt());
        teacher.setEmail(request.getEmail());
        teacher.setChuyenMon(request.getChuyenMon());
        teacher.setTrangThai(request.getTrangThai() != null && !request.getTrangThai().isBlank()
                ? request.getTrangThai()
                : "DANG_CONG_TAC");
        return teacher;
    }

    public TeacherResponse toResponse(Teacher teacher) {
        var response = new TeacherResponse();
        response.setMaGV(teacher.getMaGV());
        response.setMaSoGV(teacher.getMaSoGV());
        response.setHoTen(teacher.getHoTen());
        if (teacher.getNgaySinh() != null) {
            response.setNgaySinh(teacher.getNgaySinh().toLocalDate());
        }
        response.setSdt(teacher.getSdt());
        response.setEmail(teacher.getEmail());
        response.setChuyenMon(teacher.getChuyenMon());
        response.setTrangThai(teacher.getTrangThai());
        return response;
    }

    public void updateEntity(UpdateTeacherRequest request, Teacher teacher) {
        teacher.setHoTen(request.getHoTen());
        teacher.setNgaySinh(request.getNgaySinh());
        teacher.setSdt(request.getSdt());
        teacher.setEmail(request.getEmail());
        teacher.setChuyenMon(request.getChuyenMon());
        if (request.getTrangThai() != null && !request.getTrangThai().isBlank()) {
            teacher.setTrangThai(request.getTrangThai());
        }
    }
}
