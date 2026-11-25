package com.example.highschool_management.controller;

import com.example.highschool_management.dto.request.CreateSchoolClassRequest;
import com.example.highschool_management.dto.request.UpdateSchoolClassRequest;
import com.example.highschool_management.dto.response.ClassCatalogResponse;
import com.example.highschool_management.dto.response.PageResponse;
import com.example.highschool_management.dto.response.SchoolClassListResponse;
import com.example.highschool_management.dto.response.SchoolClassResponse;
import com.example.highschool_management.service.SchoolClassService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/classes")
public class SchoolClassController {
  private SchoolClassService schoolClassService;

  public SchoolClassController(SchoolClassService schoolClassService) {
    this.schoolClassService = schoolClassService;
  }

  @GetMapping("/{maLop}")
  @PreAuthorize("hasAnyRole('ADMIN', 'BGH', 'GIAO_VU', 'GIAO_VIEN', 'TEACHER')")
  public SchoolClassResponse getSchoolClass(@PathVariable("maLop") Long maLop) {
    return schoolClassService.getSchoolClassById(maLop);
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'BGH', 'GIAO_VU')")
  public ResponseEntity<SchoolClassResponse> them(
      @Valid @RequestBody CreateSchoolClassRequest request) {
    var ketQua = schoolClassService.them(request);
    URI diaChi = URI.create("/api/v1/classes/" + ketQua.maLop());
    return ResponseEntity.created(diaChi).body(ketQua); // check
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'BGH', 'GIAO_VU', 'GIAO_VIEN', 'TEACHER')")
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
  @PreAuthorize("hasAnyRole('ADMIN', 'BGH', 'GIAO_VU')")
  public SchoolClassResponse capNhat(
      @PathVariable("maLop") Long maLop, @Valid @RequestBody UpdateSchoolClassRequest request) {
    return schoolClassService.capNhat(maLop, request);
  }

  @DeleteMapping("/{maLop}")
  @PreAuthorize("hasAnyRole('ADMIN', 'BGH', 'GIAO_VU')")
  public ResponseEntity<Void> xoa(@PathVariable("maLop") Long maLop) {
    schoolClassService.xoaTheoMa(maLop);

    return ResponseEntity.noContent().build();
  }

  @GetMapping("/catalog")
  @PreAuthorize("hasAnyRole('ADMIN', 'BGH', 'GIAO_VU', 'GIAO_VIEN', 'TEACHER')")
  public ClassCatalogResponse danhMuc() {
    return schoolClassService.danhMuc();
  }
}
