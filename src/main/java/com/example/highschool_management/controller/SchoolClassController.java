package com.example.highschool_management.controller;

import com.example.highschool_management.dto.request.CreateSchoolClassRequest;
import com.example.highschool_management.dto.request.UpdateSchoolClassRequest;
import com.example.highschool_management.dto.response.*;
import com.example.highschool_management.service.EnrollmentService;
import com.example.highschool_management.service.SchoolClassService;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/classes")
public class SchoolClassController {
  private SchoolClassService schoolClassService;
  private final EnrollmentService enrollmentService;

  public SchoolClassController(SchoolClassService schoolClassService, EnrollmentService enrollmentService) {
    this.schoolClassService = schoolClassService;
    this.enrollmentService = enrollmentService;
  }

  @GetMapping("/{maLop}")
  public SchoolClassResponse getSchoolClass(@PathVariable("maLop") Long maLop) {
    return schoolClassService.getSchoolClassById(maLop);
  }

  @PostMapping
  public ResponseEntity<SchoolClassResponse> them(
      @Valid @RequestBody CreateSchoolClassRequest request) {
    var ketQua = schoolClassService.them(request);
    URI diaChi = URI.create("/api/v1/classes/" + ketQua.maLop());
    return ResponseEntity.created(diaChi).body(ketQua); // check
  }

  @GetMapping
  public PageResponse<SchoolClassListResponse> timDanhSach(
      @RequestParam(name = "tuKhoa", defaultValue = "") String tuKhoa,
      @RequestParam(name = "maNamHoc", required = false) Long maNamHoc,
      @RequestParam(name = "maKhoi", required = false) Long maKhoi,
      @RequestParam(name = "trangThai", defaultValue = "") String trangThai,
      @RequestParam(name = "trang", defaultValue = "0") int trang,
      @RequestParam(name = "kichThuoc", defaultValue = "10") int kichThuoc) {
    return schoolClassService.timDanhSach(tuKhoa, maNamHoc, maKhoi, trangThai, trang, kichThuoc);
  }

  @PutMapping("/{maLop}")
  public SchoolClassResponse capNhat(
      @PathVariable("maLop") Long maLop, @Valid @RequestBody UpdateSchoolClassRequest request) {
    return schoolClassService.capNhat(maLop, request);
  }

  @DeleteMapping("/{maLop}")
  public ResponseEntity<Void> xoa(@PathVariable("maLop") Long maLop) {
    schoolClassService.xoaTheoMa(maLop);

    return ResponseEntity.noContent().build();
  }

  @GetMapping("/catalog")
  public ClassCatalogResponse danhMuc() {
    return schoolClassService.danhMuc();
  }
  @GetMapping("/{id}/students")
  public ResponseEntity<PageResponse<ClassStudentResponse>> danhSachHocSinh(
          @PathVariable("id") Long maLop,

          @RequestParam(name = "ngay", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate ngay,

          @RequestParam(name = "trang", defaultValue = "0")
          int trang,

          @RequestParam(name = "kichThuoc", defaultValue = "20")
          int kichThuoc
  ) {
    PageResponse<ClassStudentResponse> response =
            enrollmentService.danhSachLop(
                    maLop,
                    ngay,
                    trang,
                    kichThuoc
            );

    return ResponseEntity.ok(response);
  }

}
