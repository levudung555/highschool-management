package com.example.highschool_management.controller;

import com.example.highschool_management.dto.ApiResponse;
import com.example.highschool_management.dto.request.CreateStudentRequest;
import com.example.highschool_management.dto.request.UpdateStudentRequest;
import com.example.highschool_management.dto.response.StudentResponse;
import com.example.highschool_management.dto.response.StudentDetailResponse;
import com.example.highschool_management.service.StudentService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/student")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class StudentController {

  StudentService studentService;

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'BGH', 'GIAO_VU')")
  public StudentResponse createUser(@RequestBody @Valid CreateStudentRequest request) {
    return studentService.createStudent(request);
  }

  @PutMapping("/{maHS}")
  @PreAuthorize("hasAnyRole('ADMIN', 'BGH', 'GIAO_VU')")
  public StudentResponse updateUser(
      @PathVariable Long maHS, @RequestBody @Valid UpdateStudentRequest request) {
    return studentService.updateStudent(maHS, request);
  }

  @GetMapping("/search")
  @PreAuthorize("hasAnyRole('ADMIN', 'BGH', 'GIAO_VU')")
  public List<StudentResponse> searchStudents(
      @RequestParam(required = false) String maSoHS,
      @RequestParam(required = false) String hoTen,
      @RequestParam(required = false) Long maLop,
      @RequestParam(required = false) Long maKhoi,
      @RequestParam(required = false) Long maNamHoc) {
    return studentService.searchStudents(maSoHS, hoTen, maLop, maKhoi, maNamHoc);
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'BGH', 'GIAO_VU')")
  public List<StudentResponse> getAllStudents() {
    return studentService.getAllStudents();
  }

  @GetMapping("/{maHS}")
  @PreAuthorize("hasAnyRole('ADMIN', 'BGH', 'GIAO_VU')")
  public StudentDetailResponse getStudentDetail(@PathVariable Long maHS) {
    return studentService.getStudentDetail(maHS);
  }

  @DeleteMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'BGH', 'GIAO_VU')")
  public boolean deleteStudent(@RequestParam List<Long> maHS) {
    return studentService.deleteStudent(maHS);
  }

  @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasAnyRole('ADMIN', 'BGH', 'GIAO_VU')")
  public ApiResponse<com.example.highschool_management.dto.response.ImportStudentResponse> importExcelStudent(
      @RequestParam("file") MultipartFile file,
      @RequestParam("maNamHoc") Long maNamHoc) throws java.io.IOException {
    return ApiResponse.<com.example.highschool_management.dto.response.ImportStudentResponse>builder()
        .result(studentService.importStudents(file, maNamHoc))
        .build();
  }
}
