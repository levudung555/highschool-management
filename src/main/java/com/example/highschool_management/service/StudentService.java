package com.example.highschool_management.service;

import com.example.highschool_management.dto.request.CreateStudentRequest;
import com.example.highschool_management.dto.request.UpdateStudentRequest;
import com.example.highschool_management.dto.response.StudentResponse;
import java.util.List;

public interface StudentService {
  StudentResponse createStudent(CreateStudentRequest request);

  List<StudentResponse> searchStudents(
      String maSoHS, String hoTen, Long maLop, Long maKhoi, Long maNamHoc);

  List<StudentResponse> getAllStudents();

  StudentResponse updateStudent(Long maHS, UpdateStudentRequest request);

  boolean deleteStudent(List<Long> maHS);
}
