package com.example.highschool_management.mapper;

import com.example.highschool_management.dto.request.CreateStudentRequest;
import com.example.highschool_management.dto.request.UpdateStudentRequest;
import com.example.highschool_management.dto.response.StudentResponse;
import com.example.highschool_management.model.Student;
import org.springframework.stereotype.Component;

@Component
public class StudentMapper {
  public Student toEntity(CreateStudentRequest request) {
    var student = new Student();
    student.setHoTen(request.getHoTen());
    student.setNgaySinh(request.getNgaySinh());
    student.setGioiTinh(request.getGioiTinh());
    student.setDiaChi(request.getDiaChi());
    student.setSdt(request.getSdt());
    student.setEmail(request.getEmail());
    return student;
  }

  public StudentResponse toResponse(Student student) {
    var response = new StudentResponse();
    response.setMaHS(student.getMaHS());
    response.setMaSoHS(student.getMaSoHS());
    response.setHoTen(student.getHoTen());
    response.setNgaySinh(
        student.getNgaySinh() == null ? null : student.getNgaySinh().toLocalDate());
    response.setGioiTinh(student.getGioiTinh());
    response.setDiaChi(student.getDiaChi());
    response.setSdt(student.getSdt());
    response.setEmail(student.getEmail());
    response.setNgayTiepNhan(
        student.getNgayTiepNhan() == null ? null : student.getNgayTiepNhan().toLocalDate());
    return response;
  }

  public void updateEntity(UpdateStudentRequest request, Student student) {
    student.setHoTen(request.getHoTen());
    student.setNgaySinh(request.getNgaySinh());
    student.setGioiTinh(request.getGioiTinh());
    student.setDiaChi(request.getDiaChi());
    student.setSdt(request.getSdt());
    student.setEmail(request.getEmail());
  }
}
