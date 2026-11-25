package com.example.highschool_management.controller;

import com.example.highschool_management.dto.request.*;
import com.example.highschool_management.dto.response.*;
import com.example.highschool_management.service.EnrollmentService;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class EnrollmentController {
  private final EnrollmentService service;

  public EnrollmentController(EnrollmentService service) {
    this.service = service;
  }

  private ResponseEntity<EnrollmentResponse> created(EnrollmentResponse result) {
    return ResponseEntity.created(URI.create("/api/v1/enrollments/" + result.maXepLop()))
        .body(result);
  }

  @GetMapping("/enrollments/batches/status")
  public Grade10PlacementStatusResponse trangThaiXepLop10(
          @RequestParam("maNamHoc") Long maNamHoc) {
    return service.trangThaiXepLop10(maNamHoc);
  }
  @GetMapping("/enrollments/batches/preview")
  public Grade10PlacementPreviewResponse xemTruocXepLop10(
          @RequestParam("maNamHoc") Long maNamHoc) {

    return service.xemTruocXepLop10(maNamHoc);
  }
  @PostMapping("/enrollments/batches/confirm")
  public Map<String, Object> chotXepLop10(
          @Valid @RequestBody ConfirmGrade10PlacementRequest yeuCau) {

    int soHocSinhDaXep = service.chotXepLop10(yeuCau);

    return Map.of(
            "maNamHoc", yeuCau.maNamHoc(),
            "ngayVaoLop", yeuCau.ngayVaoLop(),
            "soHocSinhDaXep", soHocSinhDaXep
    );
  }
  @PostMapping("/enrollments")
  @PreAuthorize("hasAnyRole('ADMIN', 'BGH', 'GIAO_VU')")
  public ResponseEntity<EnrollmentResponse> xepLop(
      @Valid @RequestBody EnrollStudentRequest request) {
    return created(service.xepLop(request));
  }

  @PostMapping("/enrollments/transfers")
  @PreAuthorize("hasAnyRole('ADMIN', 'BGH', 'GIAO_VU')")
  public ResponseEntity<EnrollmentResponse> chuyenLop(
      @Valid @RequestBody TransferStudentRequest request) {
    return created(service.chuyenLop(request));
  }

  @GetMapping("/enrollments/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'BGH', 'GIAO_VU', 'GIAO_VIEN', 'TEACHER')")
  public EnrollmentResponse chiTiet(@PathVariable("id") Long id) {
    return service.chiTiet(id);
  }

  @GetMapping("/students/{id}/enrollments")
  @PreAuthorize("hasAnyRole('ADMIN', 'BGH', 'GIAO_VU', 'GIAO_VIEN', 'TEACHER')")
  public List<EnrollmentResponse> lichSu(@PathVariable("id") Long id) {
    return service.lichSu(id);
  }

  @GetMapping("/classes/{id}/students")
  @PreAuthorize("hasAnyRole('ADMIN', 'BGH', 'GIAO_VU', 'GIAO_VIEN', 'TEACHER')")
  public PageResponse<ClassStudentResponse> danhSach(
      @PathVariable("id") Long id,
      @RequestParam(name = "ngay", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate ngay,
      @RequestParam(name = "trang", defaultValue = "0") int trang,
      @RequestParam(name = "kichThuoc", defaultValue = "20") int size) {
    return service.danhSachLop(id, ngay, trang, size);
  }

  @GetMapping("/enrollments")
  @PreAuthorize("hasAnyRole('ADMIN', 'BGH', 'GIAO_VU', 'GIAO_VIEN', 'TEACHER')")
  public PageResponse<EnrollmentRequestResponse> timYeuCau(
      @RequestParam(name = "tuKhoa", defaultValue = "") String tuKhoa,
      @RequestParam(name = "maNamHoc", required = false) Long year,
      @RequestParam(name = "trang", defaultValue = "0") int trang,
      @RequestParam(name = "kichThuoc", defaultValue = "10") int kichThuoc) {
    return service.timYeuCau(tuKhoa, year, trang, kichThuoc);
  }

  @GetMapping("/enrollments/students")
  @PreAuthorize("hasAnyRole('ADMIN', 'BGH', 'GIAO_VU', 'GIAO_VIEN', 'TEACHER')")
  public PageResponse<EnrollmentStudentResponse> timHocSinh(
      @RequestParam(name = "maNamHocChuaXep", required = false) Long year,
      @RequestParam(name = "tuKhoa", defaultValue = "") String tuKhoa,
      @RequestParam(name = "trang", defaultValue = "0") int trang,
      @RequestParam(name = "kichThuoc", defaultValue = "20") int kichThuoc) {
    return service.timHocSinh(tuKhoa, year, trang, kichThuoc);
  }
}
