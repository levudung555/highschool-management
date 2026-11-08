package com.example.highschool_management.service.impl;

import com.example.highschool_management.dao.EnrollmentDao;
import com.example.highschool_management.dao.SchoolClassDao;
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

    public EnrollmentServiceImpl(EnrollmentDao dao, SchoolClassDao classes,
            EnrollmentMapper mapper, NguoiDungHienTai nguoiDung, Clock clock) {
        this.dao = dao;
        this.classes = classes;
        this.mapper = mapper;
        this.nguoiDung = nguoiDung;
        this.clock = clock;
    }

    private long kiemTraQuyen() {
        return nguoiDung.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");
    }

    private void kiemTraMa(Long ma) {
        if (ma == null || ma <= 0) throw new InvalidRequestException("Mã phải lớn hơn 0");
    }

    private void khoaHocSinh(Long maHocSinh) {
        kiemTraMa(maHocSinh);
        if (!dao.khoaHocSinh(maHocSinh))
            throw new ResourceNotFoundException("Không tìm thấy học sinh");
    }

    private Enrollment lay(Long maXepLop) {
        kiemTraMa(maXepLop);
        return dao.timTheoMa(maXepLop)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy xếp lớp"));
    }

    private SchoolClass khoaLop(Long maLop) {
        kiemTraMa(maLop);
        return classes.timTheoMaDeCapNhat(maLop)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp"));
    }

    private LocalDate homNay() { return LocalDate.now(clock); }

    private void kiemTraLopVaNgay(SchoolClass lop, LocalDate ngay) {
        if (!"HOAT_DONG".equals(lop.getTrangThai()))
            throw new BusinessException("Lớp đã ngừng hoạt động");
        if (ngay == null || ngay.isBefore(homNay()))
            throw new InvalidRequestException("Ngày hiệu lực không được ở quá khứ");
        SchoolYearPeriodResponse nam = dao.namHoc(lop.getMaNamHoc());
        if (ngay.isBefore(nam.getBatDau()) || ngay.isAfter(nam.getKetThuc()))
            throw new InvalidRequestException("Ngày hiệu lực phải nằm trong năm học của lớp");
    }

    private Enrollment taoBanGhi(Long maHocSinh, Long maLop, LocalDate tuNgay, long nguoiThucHien) {
        Enrollment enrollment = new Enrollment();
        enrollment.setMaHocSinh(maHocSinh);
        enrollment.setMaLop(maLop);
        enrollment.setTuNgay(tuNgay);
        enrollment.setDenNgay(null);
        enrollment.setMaTaiKhoanThucHien(nguoiThucHien);
        enrollment.setThoiDiemThucHien(LocalDateTime.now(clock));
        enrollment.setMaXepLop(dao.tao(enrollment));
        return enrollment;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public EnrollmentResponse xepLop(EnrollStudentRequest request) {
        long nguoiThucHien = kiemTraQuyen();
        // Khóa học sinh trước khi kiểm tra: chặn hai người cùng xếp lần đầu.
        khoaHocSinh(request.getMaHocSinh());
        SchoolClass lop = khoaLop(request.getMaLop());
        kiemTraLopVaNgay(lop, request.getTuNgay());
        if (dao.coXepLopTrongNamHoc(request.getMaHocSinh(), lop.getMaNamHoc()))
            throw new BusinessException("Học sinh đã được xếp lớp trong năm học này");
        Enrollment enrollment = taoBanGhi(request.getMaHocSinh(), lop.getMaLop(),
            request.getTuNgay(), nguoiThucHien);
        return mapper.sangResponse(lay(enrollment.getMaXepLop()));
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public EnrollmentResponse chuyenLop(TransferStudentRequest request) {
        long nguoiThucHien = kiemTraQuyen();
        Enrollment thongTinNguon = lay(request.getMaXepLopHienTai());
        khoaHocSinh(thongTinNguon.getMaHocSinh());
        // Đọc lại sau khi chờ khóa; không dùng bản đọc sơ bộ để quyết định chuyển.
        Enrollment nguon = dao.timTheoMaDeCapNhat(request.getMaXepLopHienTai())
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phân lớp nguồn"));
        if (!nguon.getMaHocSinh().equals(thongTinNguon.getMaHocSinh()))
            throw new BusinessException("Phân lớp nguồn đã thay đổi, vui lòng tải lại");
        if (nguon.getDenNgay() != null)
            throw new BusinessException("Phân lớp đã kết thúc hoặc đã có lịch chuyển");
        if (nguon.getMaLop().equals(request.getMaLopDich()))
            throw new BusinessException("Lớp đích phải khác lớp cũ");
        kiemTraMa(request.getMaLopDich());
        // Khóa lớp theo cùng thứ tự để giảm nguy cơ hai giao dịch chờ nhau.
        Map<Long, SchoolClass> cacLop = new HashMap<>();
        Long maNho = Math.min(nguon.getMaLop(), request.getMaLopDich());
        Long maLon = Math.max(nguon.getMaLop(), request.getMaLopDich());
        cacLop.put(maNho, khoaLop(maNho));
        cacLop.put(maLon, khoaLop(maLon));
        SchoolClass lopCu = cacLop.get(nguon.getMaLop());
        SchoolClass lopMoi = cacLop.get(request.getMaLopDich());
        if (!lopCu.getMaNamHoc().equals(lopMoi.getMaNamHoc())
                || !lopCu.getMaKhoi().equals(lopMoi.getMaKhoi()))
            throw new BusinessException("Chỉ chuyển lớp trong cùng năm học và cùng khối");
        LocalDate ngayChuyen = request.getNgayChuyen();
        kiemTraLopVaNgay(lopMoi, ngayChuyen);
        if (!ngayChuyen.isAfter(nguon.getTuNgay()))
            throw new BusinessException("Ngày chuyển phải sau ngày bắt đầu lớp cũ");
        if (dao.trungThoiGian(nguon.getMaHocSinh(), lopMoi.getMaNamHoc(),
                ngayChuyen, null, nguon.getMaXepLop()))
            throw new BusinessException("Khoảng phân lớp mới bị trùng với lịch sử khác");
        String lyDo = request.getLyDo().trim();
        // DenNgay bao gồm ngày kết thúc: chuyển 15/10 thì nguồn kết thúc 14/10.
        // Cả cập nhật nguồn, thêm đích và nhật ký cùng commit hoặc cùng rollback.
        dao.ketThuc(nguon.getMaXepLop(), ngayChuyen.minusDays(1), lyDo);
        Enrollment dich = taoBanGhi(nguon.getMaHocSinh(), lopMoi.getMaLop(), ngayChuyen, nguoiThucHien);
        dao.ghiNhatKyChuyenLop(nguoiThucHien, nguon.getMaXepLop(), dich.getMaXepLop(),
            ngayChuyen, lyDo, dich.getThoiDiemThucHien());
        return mapper.sangResponse(lay(dich.getMaXepLop()));
    }

    @Override
    public EnrollmentResponse chiTiet(Long maXepLop) {
        kiemTraQuyen();
        return mapper.sangResponse(lay(maXepLop));
    }

    @Override
    public List<EnrollmentResponse> lichSu(Long maHocSinh) {
        kiemTraQuyen();
        kiemTraMa(maHocSinh);
        return dao.lichSu(maHocSinh).stream().map(mapper::sangResponse).toList();
    }

    private String kiemTraTimKiem(String tuKhoa, int trang, int kichThuoc) {
        kiemTraQuyen();
        if (trang < 0 || kichThuoc < 1 || kichThuoc > 100)
            throw new InvalidRequestException("Trang >= 0, kích thước từ 1 đến 100");
        String ketQua = tuKhoa == null ? "" : tuKhoa.trim();
        if (ketQua.length() > 100) throw new InvalidRequestException("Từ khóa tối đa 100 ký tự");
        return ketQua;
    }

    @Override
    public PageResponse<ClassStudentResponse> danhSachLop(Long maLop, LocalDate ngay, int trang, int kichThuoc) {
        kiemTraTimKiem("", trang, kichThuoc);
        kiemTraMa(maLop);
        if (classes.findById(maLop).isEmpty()) throw new ResourceNotFoundException("Không tìm thấy lớp");
        if (ngay == null) ngay = homNay();
        long tong = dao.demDanhSachLop(maLop, ngay);
        return new PageResponse<>(dao.danhSachLop(maLop, ngay, kichThuoc, (long) trang * kichThuoc),
            trang, kichThuoc, tong, (tong + kichThuoc - 1) / kichThuoc);
    }

    @Override
    public PageResponse<EnrollmentStudentResponse> timHocSinh(String tuKhoa, Long maNamHocChuaXep, int trang, int kichThuoc) {
        String tuKhoaSach = kiemTraTimKiem(tuKhoa, trang, kichThuoc);
        if (maNamHocChuaXep != null) {
            kiemTraMa(maNamHocChuaXep);
            dao.namHoc(maNamHocChuaXep);
        }
        long tong = dao.demHocSinh(tuKhoaSach, maNamHocChuaXep);
        return new PageResponse<>(dao.timHocSinh(tuKhoaSach, maNamHocChuaXep, kichThuoc, (long) trang * kichThuoc),
            trang, kichThuoc, tong, (tong + kichThuoc - 1) / kichThuoc);
    }

    @Override
    public PageResponse<EnrollmentResponse> timPhanLop(String tuKhoa, Long maNamHoc, int trang, int kichThuoc) {
        String tuKhoaSach = kiemTraTimKiem(tuKhoa, trang, kichThuoc);
        if (maNamHoc != null) { kiemTraMa(maNamHoc); dao.namHoc(maNamHoc); }
        long tong = dao.demPhanLop(tuKhoaSach, maNamHoc);
        List<EnrollmentResponse> danhSach = dao.timPhanLop(tuKhoaSach, maNamHoc, kichThuoc,
            (long) trang * kichThuoc).stream().map(mapper::sangResponse).toList();
        return new PageResponse<>(danhSach, trang, kichThuoc, tong, (tong + kichThuoc - 1) / kichThuoc);
    }
}
