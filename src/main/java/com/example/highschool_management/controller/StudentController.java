package com.example.highschool_management.controller;

import com.example.highschool_management.dto.request.CreateStudentRequest;
import com.example.highschool_management.dto.request.UpdateStudentRequest;
import com.example.highschool_management.dto.response.EnrollmentResponse;
import com.example.highschool_management.dto.response.StudentResponse;
import com.example.highschool_management.service.EnrollmentService;
import com.example.highschool_management.service.StudentService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/student")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class StudentController {

  StudentService studentService;
  private final EnrollmentService enrollmentService;


  @PostMapping
  public StudentResponse createUser(@RequestBody @Valid CreateStudentRequest request) {
    return studentService.createStudent(request);
  }

  @PutMapping("/{maHS}")
  public StudentResponse updateUser(
      @PathVariable Long maHS, @RequestBody @Valid UpdateStudentRequest request) {
    return studentService.updateStudent(maHS, request);
  }

  @GetMapping("/search")
  public List<StudentResponse> searchStudents(
      @RequestParam(required = false) String maSoHS,
      @RequestParam(required = false) String hoTen,
      @RequestParam(required = false) Long maLop,
      @RequestParam(required = false) Long maKhoi,
      @RequestParam(required = false) Long maNamHoc) {
    return studentService.searchStudents(maSoHS, hoTen, maLop, maKhoi, maNamHoc);
  }
  @GetMapping("/{id}/enrollments")
  public ResponseEntity<List<EnrollmentResponse>> lichSuPhanLop(
          @PathVariable("id") Long maHocSinh
  ) {
    List<EnrollmentResponse> response =
            enrollmentService.lichSu(maHocSinh);

    return ResponseEntity.ok(response);
  }

  @GetMapping
  public List<StudentResponse> getAllStudents() {
    return studentService.getAllStudents();
  }

  @DeleteMapping
  public boolean deleteStudent(@RequestParam List<Long> maHS) {
    return studentService.deleteStudent(maHS);
  }
}
