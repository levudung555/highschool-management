package com.example.highschool_management.service.impl;

import com.example.highschool_management.dao.*;
import com.example.highschool_management.dto.request.*;
import com.example.highschool_management.dto.response.*;
import com.example.highschool_management.exception.*;
import com.example.highschool_management.mapper.EnrollmentMapper;
import com.example.highschool_management.model.*;
import com.example.highschool_management.security.NguoiDungHienTai;
import com.example.highschool_management.service.EnrollmentService;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@Transactional(readOnly = true)
public class EnrollmentServiceImpl implements EnrollmentService {
  private final EnrollmentDao dao;
  private final SchoolClassDao classes;
  private final EnrollmentMapper mapper;
  private final NguoiDungHienTai nguoiDung;
  private final Clock clock;

  public EnrollmentServiceImpl(
      EnrollmentDao dao,
      SchoolClassDao classes,
      EnrollmentMapper mapper,
      NguoiDungHienTai nguoiDung,
      Clock clock) {
    this.dao = dao;
    this.classes = classes;
    this.mapper = mapper;
    this.nguoiDung = nguoiDung;
    this.clock = clock;
  }

  private void quyenGiaoVu() {
    nguoiDung.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");
  }

  private void kiemTraMa(Long id) {
    if (id == null || id <= 0) throw new InvalidRequestException("Mã phải lớn hơn 0");
  }

  private void khoaHocSinh(Long id) {
    kiemTraMa(id);
    if (!dao.khoaHocSinh(id)) throw new ResourceNotFoundException("Không tìm thấy học sinh: " + id);
  }

  private Enrollment lay(Long id) {
    kiemTraMa(id);
    return dao.timTheoMa(id)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy xếp lớp: " + id));
  }

  private SchoolClass khoaLop(Long id) {
    kiemTraMa(id);
    return classes
        .timTheoMaDeCapNhat(id)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp: " + id));
  }

  private Map<Long, SchoolClass> khoaCacLop(Long... ids) {
    var result = new HashMap<Long, SchoolClass>();
    Arrays.stream(ids).distinct().sorted().forEach(id -> result.put(id, khoaLop(id)));
    return result;
  }

  private LocalDate homNay() {
    return LocalDate.now(clock);
  }

  private void kiemTraNgay(LocalDate day) {
    if (day == null || day.isBefore(homNay()))
      throw new InvalidRequestException("Ngày hiệu lực không được ở quá khứ");
  }

  private void kiemTraLop(SchoolClass lop, LocalDate day) {
    if (!"HOAT_DONG".equals(lop.getTrangThai()))
      throw new BusinessException("Lớp đã ngừng hoạt động");
    var year = dao.namHoc(lop.getMaNamHoc());
    if (day.isBefore(year.batDau()) || day.isAfter(year.ketThuc()))
      throw new InvalidRequestException("Ngày hiệu lực phải nằm trong năm học của lớp");
  }

  private void kiemTraTrung(Enrollment e) {
    if (dao.trungThoiGian(
        e.getMaHocSinh(), e.getTuNgay(), e.getDenNgay(), e.getMaXepLop(), e.getMaXepLopNguon()))
      throw new BusinessException("Học sinh đã có phân lớp hoặc yêu cầu chờ duyệt trùng thời gian");
  }

  private Enrollment moi(Long hs, Long lop, LocalDate start, LocalDate end) {
    var e = new Enrollment();
    e.setMaHocSinh(hs);
    e.setMaLop(lop);
    e.setTuNgay(start);
    e.setDenNgay(end);
    e.setTrangThaiDuyet("CHO_DUYET");
    return e;
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public EnrollmentResponse xepLop(EnrollStudentRequest request) {
    quyenGiaoVu();
    kiemTraNgay(request.tuNgay());
    khoaHocSinh(request.maHocSinh());
    var lop = khoaLop(request.maLop());
    kiemTraLop(lop, request.tuNgay());
    var end = dao.namHoc(lop.getMaNamHoc()).ketThuc().plusDays(1);
    var e = moi(request.maHocSinh(), request.maLop(), request.tuNgay(), end);
    kiemTraTrung(e);

    e.setMaXepLop(dao.taoChoDuyet(e));
    return mapper.sangResponse(e);
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public EnrollmentResponse chuyenLop(TransferStudentRequest request) {
    quyenGiaoVu();
    kiemTraNgay(request.ngayChuyen());
    khoaHocSinh(request.maHocSinh());
    var sources =
        dao.lichSu(request.maHocSinh()).stream()
            .filter(
                e ->
                    "DA_DUYET".equals(e.getTrangThaiDuyet())
                        && !e.getTuNgay().isAfter(request.ngayChuyen())
                        && (e.getDenNgay() == null || e.getDenNgay().isAfter(request.ngayChuyen())))
            .toList();
    if (sources.size() != 1)
      throw new BusinessException("Cần đúng một phân lớp đã duyệt tại ngày chuyển");
    var source = sources.getFirst();
    if (!request.ngayChuyen().isAfter(source.getTuNgay()))
      throw new BusinessException("Ngày chuyển phải sau ngày bắt đầu của phân lớp cũ");
    if (source.getMaLop().equals(request.maLopDich()))
      throw new BusinessException("Lớp đích phải khác lớp cũ");
    var locked = khoaCacLop(source.getMaLop(), request.maLopDich());
    var target = locked.get(request.maLopDich());
    var old = locked.get(source.getMaLop());
    if (!old.getMaNamHoc().equals(target.getMaNamHoc())
        || !old.getMaKhoi().equals(target.getMaKhoi()))
      throw new BusinessException("Chuyển lớp chỉ trong cùng năm học và khối");
    kiemTraLop(target, request.ngayChuyen());
    var end = source.getDenNgay();
    if (end == null) end = dao.namHoc(target.getMaNamHoc()).ketThuc().plusDays(1);
    var e = moi(request.maHocSinh(), target.getMaLop(), request.ngayChuyen(), end);
    e.setMaXepLopNguon(source.getMaXepLop());
    e.setLyDoChuyen(request.lyDo().trim());
    kiemTraTrung(e);

    e.setMaXepLop(dao.taoChoDuyet(e));
    dao.luuYeuCauChuyen(e.getMaXepLop(), source.getMaXepLop(), e.getLyDoChuyen());
    return mapper.sangResponse(e);
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public EnrollmentResponse duyet(Long id) {
    return quyetDinh(id, true);
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public EnrollmentResponse tuChoi(Long id) {
    return quyetDinh(id, false);
  }

  private EnrollmentResponse quyetDinh(Long id, boolean approve) {
    long actor = nguoiDung.yeuCauVaiTro("ADMIN", "BGH");
    var hint = lay(id);
    khoaHocSinh(hint.getMaHocSinh());
    var e = lay(id);
    var status = approve ? "DA_DUYET" : "TU_CHOI";
    if (status.equals(e.getTrangThaiDuyet()))
      return mapper.sangResponse(e); // repeat: no stock/audit changes
    if (!"CHO_DUYET".equals(e.getTrangThaiDuyet()))
      throw new BusinessException("Yêu cầu đã được xử lý");
    Enrollment source = e.getMaXepLopNguon() == null ? null : lay(e.getMaXepLopNguon());
    var locked =
        source == null ? khoaCacLop(e.getMaLop()) : khoaCacLop(source.getMaLop(), e.getMaLop());
    if (approve) {
      kiemTraNgay(e.getTuNgay());
      var target = locked.get(e.getMaLop());
      kiemTraLop(target, e.getTuNgay());
      kiemTraTrung(e);

      if (source != null) {
        if (!"DA_DUYET".equals(source.getTrangThaiDuyet())
            || !source.getMaHocSinh().equals(e.getMaHocSinh())
            || !source.getTuNgay().isBefore(e.getTuNgay())
            || (source.getDenNgay() != null && !source.getDenNgay().isAfter(e.getTuNgay())))
          throw new BusinessException("Phân lớp nguồn đã thay đổi, cần tạo lại yêu cầu chuyển");
        dao.ketThuc(source.getMaXepLop(), e.getTuNgay(), e.getLyDoChuyen());
      }
    }
    var at = LocalDateTime.now(clock);
    dao.quyetDinh(id, status, actor, at);
    e.setTrangThaiDuyet(status);
    e.setMaTaiKhoanDuyet(actor);
    e.setDuyetLuc(at);
    return mapper.sangResponse(e);
  }

  @Override
  public EnrollmentResponse chiTiet(Long id) {
    quyenGiaoVu();
    return mapper.sangResponse(lay(id));
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public List<EnrollmentResponse> lichSu(Long hs) {
    quyenGiaoVu();
    khoaHocSinh(hs);
    return dao.lichSu(hs).stream().map(mapper::sangResponse).toList();
  }

  @Override
  public PageResponse<ClassStudentResponse> danhSachLop(
      Long id, LocalDate day, int page, int size) {
    quyenGiaoVu();
    kiemTraMa(id);
    if (page < 0 || size < 1 || size > 100)
      throw new InvalidRequestException("Trang >= 0, kích thước từ 1 đến 100");
    if (classes.findById(id).isEmpty())
      throw new ResourceNotFoundException("Không tìm thấy lớp: " + id);
    if (day == null) day = homNay();
    long total = dao.demDanhSachLop(id, day);
    return new PageResponse<>(
        dao.danhSachLop(id, day, size, (long) page * size),
        page,
        size,
        total,
        total / size + (total % size == 0 ? 0 : 1));
  }

  private String kiemTraTimKiem(String tuKhoa, int trang, int kichThuoc) {
    quyenGiaoVu();
    if (trang < 0 || kichThuoc < 1 || kichThuoc > 100)
      throw new InvalidRequestException("Trang >= 0, kích thước từ 1 đến 100");
    String k = tuKhoa == null ? "" : tuKhoa.trim();
    if (k.length() > 100) throw new InvalidRequestException("Từ khóa tối đa 100 ký tự");
    return k;
  }

  @Override
  public PageResponse<EnrollmentStudentResponse> timHocSinh(
      String tuKhoa, int trang, int kichThuoc) {
    String k = kiemTraTimKiem(tuKhoa, trang, kichThuoc);
    long total = dao.demHocSinh(k);
    return new PageResponse<>(
        dao.timHocSinh(k, kichThuoc, (long) trang * kichThuoc),
        trang,
        kichThuoc,
        total,
        total / kichThuoc + (total % kichThuoc == 0 ? 0 : 1));
  }

  @Override
  public PageResponse<EnrollmentRequestResponse> timYeuCau(
      String tuKhoa, String trangThai, int trang, int kichThuoc) {
    String k = kiemTraTimKiem(tuKhoa, trang, kichThuoc);
    String status = trangThai == null ? "" : trangThai;
    if (!Set.of("", "CHO_DUYET", "DA_DUYET", "TU_CHOI").contains(status))
      throw new InvalidRequestException("Trạng thái duyệt không hợp lệ");
    long total = dao.demYeuCau(k, status);
    return new PageResponse<>(
        dao.timYeuCau(k, status, kichThuoc, (long) trang * kichThuoc),
        trang,
        kichThuoc,
        total,
        total / kichThuoc + (total % kichThuoc == 0 ? 0 : 1));
  }
}
