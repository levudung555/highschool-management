package com.example.highschool_management.service.impl;

import com.example.highschool_management.dao.SchoolClassDao;
import com.example.highschool_management.dto.request.ConfirmGrade10ClassesRequest;
import com.example.highschool_management.dto.request.CreateSchoolClassRequest;
import com.example.highschool_management.dto.request.UpdateSchoolClassRequest;
import com.example.highschool_management.dto.response.*;
import com.example.highschool_management.exception.BusinessException;
import com.example.highschool_management.exception.InvalidRequestException;
import com.example.highschool_management.exception.ResourceNotFoundException;
import com.example.highschool_management.mapper.SchoolClassMapper;
import com.example.highschool_management.model.SchoolClass;
import com.example.highschool_management.security.NguoiDungHienTai;
import com.example.highschool_management.service.SchoolClassService;
import java.time.Clock;
import java.time.LocalDate;
import java.util.*;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SchoolClassServiceImpl implements SchoolClassService {
  private final SchoolClassDao schoolClassDao;
  private final SchoolClassMapper schoolClassMapper;
  private final NguoiDungHienTai nguoiDung;
  private final Clock clock;

  public SchoolClassServiceImpl(
      SchoolClassDao schoolClassDao,
      SchoolClassMapper schoolClassMapper,
      NguoiDungHienTai nguoiDung,
      Clock clock) {
    this.schoolClassDao = schoolClassDao;
    this.schoolClassMapper = schoolClassMapper;
    this.nguoiDung = nguoiDung;
    this.clock = clock;
  }

  @Override
  @Transactional(readOnly = true)
  public ClassCatalogResponse danhMuc() {
    nguoiDung.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU", "GIAO_VIEN");
    return new ClassCatalogResponse(
        schoolClassDao.danhSachNamHoc(), schoolClassDao.danhSachKhoi(), LocalDate.now(clock));
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse<SchoolClassListResponse> timDanhSach(
      String tuKhoa, Long maNamHoc, Long maKhoi, String trangThai, int trang, int kichThuoc) {
    nguoiDung.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");
    if (trang < 0 || kichThuoc < 1 || kichThuoc > 100)
      throw new InvalidRequestException("Trang >= 0, kích thước từ 1 đến 100");
    String k = tuKhoa == null ? "" : tuKhoa.trim();
    if (k.length() > 100) throw new InvalidRequestException("Từ khóa tối đa 100 ký tự");
    String status = trangThai == null ? "" : trangThai;
    if ((maNamHoc != null && maNamHoc <= 0)
        || (maKhoi != null && maKhoi <= 0)
        || !Set.of("", "HOAT_DONG", "NGUNG_HOAT_DONG").contains(status))
      throw new InvalidRequestException("Bộ lọc lớp không hợp lệ");
    long total = schoolClassDao.demDanhSach(k, maNamHoc, maKhoi, status);
    var data =
        schoolClassDao.timDanhSach(
            k, maNamHoc, maKhoi, status, LocalDate.now(clock), kichThuoc, (long) trang * kichThuoc);
    return new PageResponse<>(
        data, trang, kichThuoc, total, total / kichThuoc + (total % kichThuoc == 0 ? 0 : 1));
  }

  @Override
  public SchoolClassResponse getSchoolClassById(Long id) {
    nguoiDung.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");
    var school =
        schoolClassDao
            .findById(id)
            .orElseThrow(
                () -> {
                  return new ResourceNotFoundException("Không tìm thấy lớp có mã: " + id);
                });
    return schoolClassMapper.sangResponse(school);
  }

  @Override
  @Transactional
  public SchoolClassResponse them(CreateSchoolClassRequest request) {
    nguoiDung.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");
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
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public SchoolClassResponse capNhat(Long maLop, UpdateSchoolClassRequest request) {
    nguoiDung.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");
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
    nguoiDung.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");
    if (maLop == null || maLop <= 0) {
      throw new InvalidRequestException("Mã lớp phải lớn hơn 0");
    }

    var lop =
        schoolClassDao
            .timTheoMaDeCapNhat(maLop)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp có mã: " + maLop));
    ;

    if (schoolClassDao.tonTaiDuLieuLienQuan(maLop)) {
      throw new BusinessException("Không thể xóa lớp học này vì có dữ liệu liên quan");
    }

    int soDongBiXoa = schoolClassDao.xoaTheoMa(maLop);

    if (soDongBiXoa == 0) {
      throw new RuntimeException("Xóa lớp học thất bại, vui lòng thử lại");
    }
  }

  // Đề xuất số lớp cần mở thêm cho từng tổ hợp dựa trên số hồ sơ, chỗ đã chuẩn bị và chỉ tiêu lớp mới.
  @Override
  @Transactional(readOnly = true)
  public List<Grade10ClassProposalResponse> deXuatMoLop10(Long maNamHoc, int chiTieuLopMoi){
    nguoiDung.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");

    if (maNamHoc == null || maNamHoc <= 0) {
      throw new InvalidRequestException("Mã năm học phải lớn hơn 0");
    }
    if(chiTieuLopMoi <= 0){
        throw new InvalidRequestException("Chỉ tiêu lớp mới phải lớn hơn 0");
    }
    if(!schoolClassDao.tonTaiNamHoc(maNamHoc)){
        throw new ResourceNotFoundException("Không tìm thấy năm học có mã: " + maNamHoc);
    }
    LocalDate homNay = LocalDate.now(clock);
    return schoolClassDao.deXuatMoLop10(maNamHoc, homNay, chiTieuLopMoi);
  }

  // Kiểm tra các lớp giáo vụ chọn mở trong năm học rồi trả về danh sách mã lớp để chuẩn bị xếp học sinh.
  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public List<Long> xacNhanMoLop10(ConfirmGrade10ClassesRequest request) {
    nguoiDung.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");
    Long maNamHoc = request.maNamHoc();

    LocalDate ngayKetThuc = schoolClassDao.khoaNamHocDeMoLop(maNamHoc)
            .orElseThrow(() -> new ResourceNotFoundException(
                    "Không tìm thấy năm học có mã: " + maNamHoc));
    if(LocalDate.now(clock).isAfter(ngayKetThuc)){
        throw new InvalidRequestException("Không thể xác nhận mở lớp 10 vì năm học đã kết thúc");
    }

    Long maKhoi10 = null;
    for (var khoi : schoolClassDao.danhSachKhoi()) {
      if (khoi.soKhoi() == 10) {
        maKhoi10 = khoi.maKhoi();
        break;
      }
    }

    if (maKhoi10 == null) {
      throw new ResourceNotFoundException("Chưa khai báo khối 10");
    }

    Set<Long> maToHop = new HashSet<>();
    for(var toHop : schoolClassDao.thongKeNguyenVongTheoNam(maNamHoc)){
        maToHop.add(toHop.maToHop());
    }

    List<Long> danhSachMaLop = new ArrayList<>();
    for (var lopDeXuat : request.danhSachLop()){
      if(!maToHop.contains(lopDeXuat.maToHop())){
        throw new InvalidRequestException(
                "Tổ hợp của lớp " + lopDeXuat.tenLop()
                        + " không thuộc năm học đã chọn");
      }
      Long maLop = taoHoacDungLaiLop10(maNamHoc, maKhoi10, lopDeXuat);
      if(danhSachMaLop.contains(maLop)){
        throw new InvalidRequestException(
                "Lớp xuất hiện hai lần trong danh sách: " + lopDeXuat.tenLop());
      }
      danhSachMaLop.add(maLop);
    }
    return danhSachMaLop;
  }

  // Tạo lớp khi tên lớp chưa có trong năm học. Nếu đã có, chỉ dùng lại khi cấu hình khớp với đề xuất.
  private long taoHoacDungLaiLop10(Long maNamHoc, Long maKhoi10, ConfirmGrade10ClassesRequest.ClassItem item){
    String tenLop = item.tenLop().trim();
    var lopHienCo = schoolClassDao.timLopTheoNamVaTen(maNamHoc, tenLop);
    if (lopHienCo.isPresent()) {
      SchoolClass lop = lopHienCo.get();
      boolean khopCauHinh =
              Objects.equals(lop.getMaKhoi(), maKhoi10)
              && Objects.equals(lop.getMaToHop(), item.maToHop())
              && Objects.equals(lop.getSiSoToiDa(), item.siSoToiDa())
              && Objects.equals(lop.getThuTuXep(), item.thuTuXep())
              && "HOAT_DONG".equals(lop.getTrangThai());
      if (!khopCauHinh) {
        throw new BusinessException(
                "Lớp " + tenLop
                        + " đã tồn tại nhưng cấu hình khác phương án đã duyệt");
      }
      return lop.getMaLop();
    }

    SchoolClass lop = new SchoolClass();
    lop.setMaNamHoc(maNamHoc);
    lop.setMaKhoi(maKhoi10);
    lop.setTenLop(tenLop);
    lop.setMaToHop(item.maToHop());
    lop.setTrangThai("HOAT_DONG");
    lop.setSiSoToiDa(item.siSoToiDa());
    lop.setThuTuXep(item.thuTuXep());

    try{
      return schoolClassDao.themLop(lop);
    } catch (DuplicateKeyException e) {
      throw new BusinessException(
              "Lớp " + tenLop
                      + "vì tên lớp hoặc thứ tự xếp đã được sử dụng");
    }
  }

}
