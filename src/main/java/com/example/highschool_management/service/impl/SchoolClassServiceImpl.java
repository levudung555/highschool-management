package com.example.highschool_management.service.impl;

import com.example.highschool_management.dao.SchoolClassDao;
import com.example.highschool_management.dto.request.CreateSchoolClassRequest;
import com.example.highschool_management.dto.request.UpdateSchoolClassRequest;
import com.example.highschool_management.dto.response.PageResponse;
import com.example.highschool_management.dto.response.SchoolClassResponse;
import com.example.highschool_management.exception.BusinessException;
import com.example.highschool_management.exception.InvalidRequestException;
import com.example.highschool_management.exception.ResourceNotFoundException;
import com.example.highschool_management.mapper.SchoolClassMapper;
import com.example.highschool_management.model.SchoolClass;
import com.example.highschool_management.service.SchoolClassService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SchoolClassServiceImpl implements SchoolClassService {
  private final SchoolClassDao schoolClassDao;
  private final SchoolClassMapper schoolClassMapper;

  public SchoolClassServiceImpl(
      SchoolClassDao schoolClassDao, SchoolClassMapper schoolClassMapper) {
    this.schoolClassDao = schoolClassDao;
    this.schoolClassMapper = schoolClassMapper;
  }

  @Override
  public SchoolClassResponse getSchoolClassById(Long id) {
    var school =
        schoolClassDao
            .findById(id)
            .orElseThrow(
                () -> {
                  return new RuntimeException("School class not found");
                });
    return schoolClassMapper.sangResponse(school);
  }

  @Override
  @Transactional
  public SchoolClassResponse them(CreateSchoolClassRequest request) {
    if (!schoolClassDao.tonTaiNamHoc(request.maNamHoc())) {
      throw new ResourceNotFoundException("Không tìm thấy mã năm học: " + request.maNamHoc());
    }
    if (!schoolClassDao.tonTaiKhoi(request.maKhoi())) {
      throw new ResourceNotFoundException("Không tìm thấy mã khối: " + request.maKhoi());
    }
    String tenLop = request.tenLop().trim();
    if (schoolClassDao.tonTaiLop(tenLop, request.maNamHoc())) {
      throw new BusinessException("Lớp học đã tồn tại trong năm học: " + request.maNamHoc());
    }
    SchoolClass lop = new SchoolClass();
    lop.setMaKhoi(request.maKhoi());
    lop.setMaNamHoc(request.maNamHoc());
    lop.setTenLop(tenLop);
    lop.setTrangThai("HOAT_DONG");
    Long maLop;
    try {
      maLop = schoolClassDao.themLop(lop);
    } catch (DuplicateKeyException e) {
      throw new BusinessException("Lớp học đã tồn tại trong năm học: " + request.maNamHoc());
    }
    lop.setMaLop(maLop);
    return schoolClassMapper.sangResponse(lop);
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse<SchoolClassResponse> timDanhSach(String tuKhoa, int trang, int kichThuoc) {
    if (trang < 0) {
      throw new InvalidRequestException("Số trang phải lớn hơn hoặc bằng 0");
    }
    if (kichThuoc < 1 || kichThuoc > 100) {
      throw new InvalidRequestException("Kích thước trang phải từ 1 đến 100");
    }
    String tuKhoaChuanHoa = tuKhoa == null ? "" : tuKhoa.trim();
    if (tuKhoaChuanHoa.length() > 100) {
      throw new InvalidRequestException("Từ khóa tìm kiếm không được vượt quá 100 ký tự");
    }
    long tongSoPhanTu = schoolClassDao.demDanhSach(tuKhoaChuanHoa);
    long viTriBatDau = (long) trang * kichThuoc;
    var danhSachLop = schoolClassDao.timDanhSach(tuKhoaChuanHoa, kichThuoc, viTriBatDau);
    var danhSachLopResponse = danhSachLop.stream().map(schoolClassMapper::sangResponse).toList();
    long tongSoTrang = tongSoPhanTu / kichThuoc;
    if (tongSoPhanTu % kichThuoc != 0) {
      tongSoTrang++;
    }
    return new PageResponse<>(danhSachLopResponse, trang, kichThuoc, tongSoPhanTu, tongSoTrang);
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public SchoolClassResponse capNhat(Long maLop, UpdateSchoolClassRequest request) {
    if (maLop == null || maLop <= 0) {
      throw new InvalidRequestException("Mã lớp phải lớn hơn 0");
    }

    var lop =
        schoolClassDao
            .timTheoMaDeCapNhat(maLop)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp có mã: " + maLop));

    String tenLop = request.tenLop().trim();

    if (schoolClassDao.tonTaiLopKhac(maLop, tenLop, lop.getMaNamHoc())) {
      throw new BusinessException("Tên lớp đã tồn tại trong năm học này");
    }

    lop.setTenLop(tenLop);
    lop.setTrangThai(request.trangThai());

    try {
      schoolClassDao.capNhat(lop);
    } catch (DuplicateKeyException exception) {
      throw new BusinessException("Tên lớp đã tồn tại trong năm học này");
    }

    return schoolClassMapper.sangResponse(lop);
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public void xoaTheoMa(Long maLop) {
    if (maLop == null || maLop <= 0) {
      throw new InvalidRequestException("Mã lớp phải lớn hơn 0");
    }

    var lop =
        schoolClassDao.timTheoMaDeCapNhat(maLop).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp có mã: " + maLop));
    ;

    if (schoolClassDao.tonTaiDuLieuLienQuan(maLop)) {
      throw new BusinessException("Không thể xóa lớp học này vì có dữ liệu liên quan");
    }

    int soDongBiXoa = schoolClassDao.xoaTheoMa(maLop);

    if (soDongBiXoa == 0) {
      throw new RuntimeException("Xóa lớp học thất bại, vui lòng thử lại");
    }
  }
}
