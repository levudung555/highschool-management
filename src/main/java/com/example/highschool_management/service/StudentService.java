package com.example.highschool_management.service;

import com.example.highschool_management.dto.request.CreateStudentRequest;
import com.example.highschool_management.dto.request.UpdateStudentRequest;
import com.example.highschool_management.dto.response.ImportStudentResponse;
import com.example.highschool_management.dto.response.StudentResponse;
import com.example.highschool_management.dto.response.StudentDetailResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface StudentService {
  List<StudentResponse> getAllStudents();

  StudentDetailResponse getStudentDetail(Long maHS);


  List<StudentResponse> searchStudents(
      String maSoHS, String hoTen, Long maLop, Long maKhoi, Long maNamHoc);

  StudentResponse createStudent(CreateStudentRequest request);

  StudentResponse updateStudent(Long maHS, UpdateStudentRequest request);

  boolean deleteStudent(List<Long> maHS);

  ImportStudentResponse importStudents(MultipartFile file, Long maNamHoc) throws IOException;
}
