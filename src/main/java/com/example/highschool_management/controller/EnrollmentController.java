package com.example.highschool_management.controller;

import com.example.highschool_management.dto.request.*;
import com.example.highschool_management.dto.response.*;
import com.example.highschool_management.service.EnrollmentService;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class EnrollmentController {
  private final EnrollmentService service;

  public EnrollmentController(EnrollmentService service) {
    this.service = service;
  }

  private ResponseEntity<EnrollmentResponse> created(EnrollmentResponse result) {
    return ResponseEntity.created(URI.create("/api/v1/enrollments/" + result.getMaXepLop()))
        .body(result);
  }

  @PostMapping("/enrollments")
  public ResponseEntity<EnrollmentResponse> xepLop(
      @Valid @RequestBody EnrollStudentRequest request) {
    return created(service.xepLop(request));
  }

  @PostMapping("/enrollments/transfers")
  public ResponseEntity<EnrollmentResponse> chuyenLop(
      @Valid @RequestBody TransferStudentRequest request) {
    return created(service.chuyenLop(request));
  }

  @GetMapping("/enrollments/{id}")
  public EnrollmentResponse chiTiet(@PathVariable("id") Long id) {
    return service.chiTiet(id);
  }

  @GetMapping("/students/{id}/enrollments")
  public List<EnrollmentResponse> lichSu(@PathVariable("id") Long id) {
    return service.lichSu(id);
  }

  @GetMapping("/classes/{id}/students")
  public PageResponse<ClassStudentResponse> danhSach(
      @PathVariable("id") Long id,
      @RequestParam(name = "ngay", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate ngay,
      @RequestParam(name = "trang", defaultValue = "0") int trang,
      @RequestParam(name = "kichThuoc", defaultValue = "20") int size) {
    return service.danhSachLop(id, ngay, trang, size);
  }

  @GetMapping("/enrollments")
  public PageResponse<EnrollmentResponse> timPhanLop(
      @RequestParam(name = "tuKhoa", defaultValue = "") String tuKhoa,
      @RequestParam(name = "maNamHoc", required = false) Long maNamHoc,
      @RequestParam(name = "trang", defaultValue = "0") int trang,
      @RequestParam(name = "kichThuoc", defaultValue = "10") int kichThuoc) {
    return service.timPhanLop(tuKhoa, maNamHoc, trang, kichThuoc);
  }

  @GetMapping("/enrollments/students")
  public PageResponse<EnrollmentStudentResponse> timHocSinh(
      @RequestParam(name = "maNamHocChuaXep", required = false) Long maNamHocChuaXep,
      @RequestParam(name = "tuKhoa", defaultValue = "") String tuKhoa,
      @RequestParam(name = "trang", defaultValue = "0") int trang,
      @RequestParam(name = "kichThuoc", defaultValue = "20") int kichThuoc) {
    return service.timHocSinh(tuKhoa, maNamHocChuaXep, trang, kichThuoc);
  }
}
