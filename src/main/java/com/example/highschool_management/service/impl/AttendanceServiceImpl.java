package com.example.highschool_management.service.impl;

import com.example.highschool_management.dao.AttendanceDao;
import com.example.highschool_management.dao.AuthDao;
import com.example.highschool_management.dao.SchoolClassDao;
import com.example.highschool_management.dto.request.AttendanceItemRequest;
import com.example.highschool_management.dto.request.CreateAttendanceSessionRequest;
import com.example.highschool_management.dto.request.UpdateAttendanceSessionRequest;
import com.example.highschool_management.dto.response.AttendancePermissionClassResponse;
import com.example.highschool_management.dto.response.AttendanceSessionResponse;
import com.example.highschool_management.dto.response.AttendanceStudentResponse;
import com.example.highschool_management.exception.AccessException;
import com.example.highschool_management.exception.BusinessException;
import com.example.highschool_management.exception.InvalidRequestException;
import com.example.highschool_management.exception.ResourceNotFoundException;
import com.example.highschool_management.model.AttendanceRecord;
import com.example.highschool_management.model.AttendanceSession;
import com.example.highschool_management.model.SchoolClass;
import com.example.highschool_management.model.UserAccount;
import com.example.highschool_management.security.NguoiDungHienTai;
import com.example.highschool_management.service.AttendanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceDao attendanceDao;
    private final SchoolClassDao schoolClassDao;
    private final AuthDao authDao;
    private final NguoiDungHienTai nguoiDungHienTai;
    private final Clock clock;

    private static final Set<String> VALID_STATUSES = Set.of("CO_MAT", "VANG_CO_PHEP", "VANG_KHONG_PHEP", "DI_MUON");
    private static final Set<String> VALID_BUOI_FRAMES = Set.of("SANG", "CHIEU", "TOI");

    public AttendanceServiceImpl(AttendanceDao attendanceDao,
                                 SchoolClassDao schoolClassDao,
                                 AuthDao authDao,
                                 NguoiDungHienTai nguoiDungHienTai,
                                 Clock clock) {
        this.attendanceDao = attendanceDao;
        this.schoolClassDao = schoolClassDao;
        this.authDao = authDao;
        this.nguoiDungHienTai = nguoiDungHienTai;
        this.clock = clock;
    }

    private void kiemTraQuyenDiemDanh(Long maTK, Long maLop, Long maLMH, LocalDate ngay) {
        List<String> vaiTro = authDao.vaiTro(maTK);
        boolean laQuanTri = vaiTro.contains("ADMIN") || vaiTro.contains("BGH") || vaiTro.contains("GIAO_VU");
        if (laQuanTri) {
            return;
        }

        if (vaiTro.contains("GIAO_VIEN")) {
            Long maGV = attendanceDao.timMaGiaoVienTheoMaTK(maTK)
                    .orElseThrow(() -> new AccessException(403, "Tài khoản giáo viên không liên kết với thông tin giáo viên"));

            // Kiểm tra chủ nhiệm
            boolean laChuNhiem = attendanceDao.laChuNhiemLop(maGV, maLop, ngay);
            if (laChuNhiem) {
                return;
            }

            // Kiểm tra giáo viên bộ môn
            boolean laBoMon = attendanceDao.laGiaoVienBoMon(maGV, maLop, maLMH, ngay);
            if (laBoMon) {
                return;
            }

            throw new AccessException(403, "Giáo viên không được phân công chủ nhiệm hoặc giảng dạy lớp này tại ngày điểm danh");
        }

        throw new AccessException(403, "Không có quyền thực hiện điểm danh");
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendancePermissionClassResponse> layDanhSachLopDuocDiemDanh(LocalDate ngay) {
        long maTK = nguoiDungHienTai.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU", "GIAO_VIEN");
        LocalDate ngayTraCuu = (ngay != null) ? ngay : LocalDate.now(clock);

        List<String> vaiTro = authDao.vaiTro(maTK);
        boolean laQuanTri = vaiTro.contains("ADMIN") || vaiTro.contains("BGH") || vaiTro.contains("GIAO_VU");

        if (laQuanTri) {
            return attendanceDao.layDanhSachLopQuanTri();
        } else {
            Long maGV = attendanceDao.timMaGiaoVienTheoMaTK(maTK)
                    .orElseThrow(() -> new AccessException(403, "Tài khoản giáo viên chưa liên kết thông tin giáo viên"));
            return attendanceDao.layDanhSachLopGiaoVien(maGV, ngayTraCuu);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceSessionResponse layThongTinDiemDanh(Long maLop, LocalDate ngay, String phamVi, String maKhung, Long maLMH) {
        long maTK = nguoiDungHienTai.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU", "GIAO_VIEN");

        if (maLop == null || maLop <= 0) {
            throw new InvalidRequestException("Mã lớp không được để trống");
        }

        SchoolClass lop = schoolClassDao.findById(maLop)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học có mã: " + maLop));

        if (!"HOAT_DONG".equals(lop.getTrangThai())) {
            throw new BusinessException("Lớp học đã ngừng hoạt động, không thể điểm danh");
        }

        LocalDate ngayDiemDanh = (ngay != null) ? ngay : LocalDate.now(clock);
        LocalDate homNay = LocalDate.now(clock);
        if (ngayDiemDanh.isAfter(homNay)) {
            throw new InvalidRequestException("Ngày điểm danh không được ở tương lai");
        }

        // Chuẩn hóa phạm vi và mã khung
        String pv = (phamVi != null && !phamVi.isBlank()) ? phamVi.trim().toUpperCase() : "BUOI";
        String khung = (maKhung != null && !maKhung.isBlank()) ? maKhung.trim().toUpperCase() : "SANG";

        if ("BUOI".equals(pv) && !VALID_BUOI_FRAMES.contains(khung)) {
            khung = "SANG";
        }

        kiemTraQuyenDiemDanh(maTK, maLop, maLMH, ngayDiemDanh);

        // Kiểm tra xem đã có phiên điểm danh chưa
        Optional<AttendanceSession> phienOpt = attendanceDao.timPhienDiemDanh(maLop, ngayDiemDanh, pv, khung);

        Long maHK = attendanceDao.timHocKyTheoNgayVaLop(maLop, ngayDiemDanh)
                .orElse(null);

        if (phienOpt.isPresent()) {
            AttendanceSession phien = phienOpt.get();
            List<AttendanceStudentResponse> chiTiet = attendanceDao.layChiTietDiemDanhTheoPhien(phien.getMaPDD());

            String nguoiTao = authDao.timTheoMa(phien.getMaTKTao()).map(UserAccount::hoTen).orElse("");
            boolean daKhoa = "DA_CHOT".equalsIgnoreCase(phien.getTrangThai());

            return AttendanceSessionResponse.builder()
                    .maPDD(phien.getMaPDD())
                    .maLop(phien.getMaLop())
                    .tenLop(lop.getTenLop())
                    .maHK(phien.getMaHK())
                    .maNH(lop.getMaNamHoc())
                    .maLMH(phien.getMaLMH())
                    .ngay(phien.getNgay())
                    .phamVi(phien.getPhamVi())
                    .maKhung(phien.getMaKhung())
                    .trangThai(phien.getTrangThai())
                    .daKhoa(daKhoa)
                    .maTKTao(phien.getMaTKTao())
                    .nguoiTao(nguoiTao)
                    .danhSachHocSinh(chiTiet)
                    .build();
        } else {
            // Chưa có phiên điểm danh -> Lấy danh sách học sinh có hiệu lực tại ngày điểm danh, mặc định CO_MAT
            List<AttendanceStudentResponse> dsHocSinh = attendanceDao.layDanhSachHocSinhTheoLopVaNgay(maLop, ngayDiemDanh);

            return AttendanceSessionResponse.builder()
                    .maPDD(null)
                    .maLop(maLop)
                    .tenLop(lop.getTenLop())
                    .maHK(maHK)
                    .maNH(lop.getMaNamHoc())
                    .maLMH(maLMH)
                    .ngay(ngayDiemDanh)
                    .phamVi(pv)
                    .maKhung(khung)
                    .trangThai("CHUA_TAO")
                    .daKhoa(false)
                    .danhSachHocSinh(dsHocSinh)
                    .build();
        }
    }

    @Override
    @Transactional
    public AttendanceSessionResponse taoPhienDiemDanh(CreateAttendanceSessionRequest request) {
        long maTK = nguoiDungHienTai.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU", "GIAO_VIEN");

        if (request.getMaLop() == null || request.getMaLop() <= 0) {
            throw new InvalidRequestException("Mã lớp không được để trống");
        }
        if (request.getNgay() == null) {
            throw new InvalidRequestException("Ngày điểm danh không được để trống");
        }
        LocalDate homNay = LocalDate.now(clock);
        if (request.getNgay().isAfter(homNay)) {
            throw new InvalidRequestException("Ngày điểm danh không được ở tương lai");
        }

        SchoolClass lop = schoolClassDao.findById(request.getMaLop())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học có mã: " + request.getMaLop()));

        if (!"HOAT_DONG".equals(lop.getTrangThai())) {
            throw new BusinessException("Lớp học đã ngừng hoạt động, không thể điểm danh");
        }

        String phamVi = (request.getPhamVi() != null && !request.getPhamVi().isBlank())
                ? request.getPhamVi().trim().toUpperCase() : "BUOI";
        String maKhung = request.getMaKhung() != null ? request.getMaKhung().trim().toUpperCase() : "SANG";

        if ("BUOI".equals(phamVi) && !VALID_BUOI_FRAMES.contains(maKhung)) {
            throw new InvalidRequestException("Mã khung buổi học không hợp lệ. Phải là SANG, CHIEU hoặc TOI");
        }
        if ("TIET".equals(phamVi) && request.getMaLMH() == null) {
            throw new InvalidRequestException("Điểm danh theo tiết yêu cầu mã lớp môn học (MaLMH)");
        }
        if (!"TIET".equals(phamVi) && request.getMaLMH() != null) {
            request.setMaLMH(null);
        }

        kiemTraQuyenDiemDanh(maTK, request.getMaLop(), request.getMaLMH(), request.getNgay());

        // Kiểm tra xem phiên đã tồn tại chưa
        Optional<AttendanceSession> tonTai = attendanceDao.timPhienDiemDanh(request.getMaLop(), request.getNgay(), phamVi, maKhung);
        if (tonTai.isPresent()) {
            throw new BusinessException("Phiên điểm danh cho lớp " + lop.getTenLop() + " ngày " + request.getNgay() + " khung " + maKhung + " đã tồn tại");
        }

        Long maHK = request.getMaHK();
        if (maHK == null) {
            maHK = attendanceDao.timHocKyTheoNgayVaLop(request.getMaLop(), request.getNgay())
                    .orElseThrow(() -> new InvalidRequestException("Ngày điểm danh không thuộc học kỳ nào hợp lệ của năm học"));
        }

        // Lấy danh sách học sinh hợp lệ của lớp tại ngày đó
        List<AttendanceStudentResponse> validStudents = attendanceDao.layDanhSachHocSinhTheoLopVaNgay(request.getMaLop(), request.getNgay());
        if (validStudents.isEmpty()) {
            throw new BusinessException("Lớp không có học sinh nào có hiệu lực xếp lớp tại ngày " + request.getNgay());
        }

        Set<Long> validMaXLSet = validStudents.stream().map(AttendanceStudentResponse::getMaXL).collect(Collectors.toSet());

        // Kiểm tra từng học sinh gửi lên
        if (request.getChiTiet() == null || request.getChiTiet().isEmpty()) {
            throw new InvalidRequestException("Danh sách chi tiết điểm danh không được để trống");
        }

        List<AttendanceRecord> recordsToInsert = new ArrayList<>();
        LocalDateTime thoiDiem = LocalDateTime.now(clock);

        for (AttendanceItemRequest item : request.getChiTiet()) {
            if (!validMaXLSet.contains(item.getMaXL())) {
                throw new BusinessException("Học sinh với mã xếp lớp " + item.getMaXL() + " không thuộc lớp tại ngày điểm danh");
            }
            if (item.getTrangThai() == null || !VALID_STATUSES.contains(item.getTrangThai())) {
                throw new InvalidRequestException("Trạng thái điểm danh không hợp lệ: " + item.getTrangThai());
            }
            if (item.getGhiChu() != null && item.getGhiChu().length() > 255) {
                throw new InvalidRequestException("Ghi chú của học sinh không được vượt quá 255 ký tự");
            }

            String dbTrangThai = "DI_MUON".equals(item.getTrangThai()) ? "CO_MAT" : item.getTrangThai();
            int soPhutMuon = "DI_MUON".equals(item.getTrangThai()) ? 15 : 0;

            recordsToInsert.add(AttendanceRecord.builder()
                    .maXL(item.getMaXL())
                    .trangThai(dbTrangThai)
                    .soPhutMuon(soPhutMuon)
                    .ghiChu(item.getGhiChu() != null ? item.getGhiChu().trim() : "")
                    .maTKCapNhat(maTK)
                    .capNhatLuc(thoiDiem)
                    .build());
        }

        // 1. Tạo phiên điểm danh
        AttendanceSession session = AttendanceSession.builder()
                .maLop(request.getMaLop())
                .maHK(maHK)
                .maLMH(request.getMaLMH())
                .ngay(request.getNgay())
                .phamVi(phamVi)
                .maKhung(maKhung)
                .trangThai("DANG_NHAP")
                .maTKTao(maTK)
                .build();

        Long maPDD = attendanceDao.taoPhienDiemDanh(session);

        // 2. Gán maPDD cho từng record và lưu
        for (AttendanceRecord r : recordsToInsert) {
            r.setMaPDD(maPDD);
        }
        attendanceDao.luuChiTietDiemDanh(recordsToInsert);

        // 3. Ghi nhật ký
        String duLieuSau = String.format("{\"MaPDD\":%d,\"SoHocSinh\":%d,\"PhamVi\":\"%s\",\"MaKhung\":\"%s\"}",
                maPDD, recordsToInsert.size(), phamVi, maKhung);
        attendanceDao.ghiNhatKy(maTK, "TAO_PHIEN_DIEM_DANH", "PHIEN_DIEM_DANH", maPDD.toString(), null, duLieuSau, thoiDiem);

        // 4. Trả về thông tin đầy đủ
        return layThongTinDiemDanh(request.getMaLop(), request.getNgay(), phamVi, maKhung, request.getMaLMH());
    }

    @Override
    @Transactional
    public AttendanceSessionResponse capNhatPhienDiemDanh(Long maPDD, UpdateAttendanceSessionRequest request) {
        long maTK = nguoiDungHienTai.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU", "GIAO_VIEN");

        if (maPDD == null || maPDD <= 0) {
            throw new InvalidRequestException("Mã phiên điểm danh không hợp lệ");
        }

        AttendanceSession phien = attendanceDao.timPhienDiemDanhTheoId(maPDD)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiên điểm danh có mã: " + maPDD));

        if ("DA_CHOT".equalsIgnoreCase(phien.getTrangThai())) {
            throw new BusinessException("Phiên điểm danh đã chốt sổ/khóa, không được phép chỉnh sửa");
        }

        SchoolClass lop = schoolClassDao.findById(phien.getMaLop())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học có mã: " + phien.getMaLop()));

        kiemTraQuyenDiemDanh(maTK, phien.getMaLop(), phien.getMaLMH(), phien.getNgay());

        // Lấy danh sách học sinh hợp lệ của phiên hiện tại
        List<AttendanceRecord> recordsCu = attendanceDao.layDiemDanhHienTaiTheoPhien(maPDD);
        Set<Long> validMaXLSet = recordsCu.stream().map(AttendanceRecord::getMaXL).collect(Collectors.toSet());

        if (request.getChiTiet() == null || request.getChiTiet().isEmpty()) {
            throw new InvalidRequestException("Danh sách chi tiết điểm danh không được để trống");
        }

        LocalDateTime thoiDiem = LocalDateTime.now(clock);
        List<AttendanceRecord> recordsToUpdate = new ArrayList<>();

        for (AttendanceItemRequest item : request.getChiTiet()) {
            if (!validMaXLSet.contains(item.getMaXL())) {
                throw new BusinessException("Học sinh với mã xếp lớp " + item.getMaXL() + " không thuộc phiên điểm danh này");
            }
            if (item.getTrangThai() == null || !VALID_STATUSES.contains(item.getTrangThai())) {
                throw new InvalidRequestException("Trạng thái điểm danh không hợp lệ: " + item.getTrangThai());
            }
            if (item.getGhiChu() != null && item.getGhiChu().length() > 255) {
                throw new InvalidRequestException("Ghi chú của học sinh không được vượt quá 255 ký tự");
            }

            String dbTrangThai = "DI_MUON".equals(item.getTrangThai()) ? "CO_MAT" : item.getTrangThai();
            int soPhutMuon = "DI_MUON".equals(item.getTrangThai()) ? 15 : 0;

            recordsToUpdate.add(AttendanceRecord.builder()
                    .maPDD(maPDD)
                    .maXL(item.getMaXL())
                    .trangThai(dbTrangThai)
                    .soPhutMuon(soPhutMuon)
                    .ghiChu(item.getGhiChu() != null ? item.getGhiChu().trim() : "")
                    .maTKCapNhat(maTK)
                    .capNhatLuc(thoiDiem)
                    .build());
        }

        // Cập nhật chi tiết
        attendanceDao.capNhatChiTietDiemDanh(maPDD, recordsToUpdate);

        // Ghi nhật ký chỉnh sửa
        String duLieuTruoc = String.format("{\"MaPDD\":%d,\"SoHocSinh\":%d}", maPDD, recordsCu.size());
        String duLieuSau = String.format("{\"MaPDD\":%d,\"SoHocSinhCapNhat\":%d}", maPDD, recordsToUpdate.size());
        attendanceDao.ghiNhatKy(maTK, "CAP_NHAT_DIEM_DANH", "PHIEN_DIEM_DANH", maPDD.toString(), duLieuTruoc, duLieuSau, thoiDiem);

        return layThongTinDiemDanh(phien.getMaLop(), phien.getNgay(), phien.getPhamVi(), phien.getMaKhung(), phien.getMaLMH());
    }
}
