package com.example.highschool_management.service;

import com.example.highschool_management.dao.AttendanceDao;
import com.example.highschool_management.dao.UserDao;
import com.example.highschool_management.dao.SchoolClassDao;
import com.example.highschool_management.dto.request.AttendanceItemRequest;
import com.example.highschool_management.dto.request.CreateAttendanceSessionRequest;
import com.example.highschool_management.dto.request.UpdateAttendanceSessionRequest;
import com.example.highschool_management.dto.response.AttendanceSessionResponse;
import com.example.highschool_management.dto.response.AttendanceStudentResponse;
import com.example.highschool_management.exception.AccessException;
import com.example.highschool_management.exception.BusinessException;
import com.example.highschool_management.exception.InvalidRequestException;
import com.example.highschool_management.exception.ResourceNotFoundException;
import com.example.highschool_management.model.AttendanceRecord;
import com.example.highschool_management.model.AttendanceSession;
import com.example.highschool_management.model.SchoolClass;
import com.example.highschool_management.security.NguoiDungHienTai;
import com.example.highschool_management.service.impl.AttendanceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AttendanceServiceTest {

    @Mock
    private AttendanceDao attendanceDao;
    @Mock
    private SchoolClassDao schoolClassDao;
    @Mock
    private UserDao userDao;
    @Mock
    private NguoiDungHienTai nguoiDungHienTai;

    private Clock fixedClock;
    private AttendanceServiceImpl attendanceService;

    private final LocalDate today = LocalDate.of(2026, 11, 18);

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2026-11-18T10:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));
        attendanceService = new AttendanceServiceImpl(attendanceDao, schoolClassDao, userDao, nguoiDungHienTai, fixedClock);
        when(nguoiDungHienTai.yeuCauVaiTro(any(String[].class))).thenReturn(2L);
        when(userDao.findRoleNames(2L)).thenReturn(List.of("GIAO_VU"));
    }

    private SchoolClass mockLop(Long maLop, String tenLop, String trangThai) {
        SchoolClass lop = new SchoolClass();
        lop.setMaLop(maLop);
        lop.setTenLop(tenLop);
        lop.setTrangThai(trangThai);
        lop.setMaNamHoc(1L);
        lop.setMaKhoi(1L);
        return lop;
    }

    @Test
    @DisplayName("1. Lấy danh sách học sinh theo lớp và ngày - Mặc định CO_MAT khi chưa có phiên")
    void testLayDanhSachHocSinh_MacDinhCoMat() {
        when(nguoiDungHienTai.yeuCauVaiTro(any())).thenReturn(2L);
        when(userDao.findRoleNames(2L)).thenReturn(List.of("GIAO_VU"));
        when(schoolClassDao.findById(1L)).thenReturn(Optional.of(mockLop(1L, "10A1", "HOAT_DONG")));
        when(attendanceDao.timPhienDiemDanh(1L, today, "BUOI", "SANG")).thenReturn(Optional.empty());
        when(attendanceDao.timHocKyTheoNgayVaLop(1L, today)).thenReturn(Optional.of(10L));

        AttendanceStudentResponse hs1 = AttendanceStudentResponse.builder()
                .maXL(101L).maHS(1L).maSoHS("HS001").hoTen("Nguyen Van A").ngaySinh(LocalDate.of(2008, 1, 1))
                .trangThai("CO_MAT").ghiChu("").build();
        when(attendanceDao.layDanhSachHocSinhTheoLopVaNgay(1L, today)).thenReturn(List.of(hs1));

        AttendanceSessionResponse res = attendanceService.layThongTinDiemDanh(1L, today, "BUOI", "SANG", null);

        assertNotNull(res);
        assertNull(res.getMaPDD());
        assertEquals("CHUA_TAO", res.getTrangThai());
        assertEquals(1, res.getDanhSachHocSinh().size());
        assertEquals("CO_MAT", res.getDanhSachHocSinh().get(0).getTrangThai());
    }

    @Test
    @DisplayName("2. Tạo phiên điểm danh thành công với transaction lưu chi tiết")
    void testTaoPhienDiemDanh_ThanhCong() {
        when(nguoiDungHienTai.yeuCauVaiTro(any())).thenReturn(2L);
        when(userDao.findRoleNames(2L)).thenReturn(List.of("GIAO_VU"));
        when(schoolClassDao.findById(1L)).thenReturn(Optional.of(mockLop(1L, "10A1", "HOAT_DONG")));
        when(attendanceDao.timPhienDiemDanh(1L, today, "BUOI", "SANG")).thenReturn(Optional.empty());
        when(attendanceDao.timHocKyTheoNgayVaLop(1L, today)).thenReturn(Optional.of(10L));

        AttendanceStudentResponse hs = AttendanceStudentResponse.builder()
                .maXL(101L).maHS(1L).maSoHS("HS001").hoTen("Nguyen Van A").build();
        when(attendanceDao.layDanhSachHocSinhTheoLopVaNgay(1L, today)).thenReturn(List.of(hs));
        when(attendanceDao.taoPhienDiemDanh(any())).thenReturn(500L);

        // Sau khi tạo xong, phương thức layThongTinDiemDanh được gọi lại để trả kết quả
        AttendanceSession savedSession = AttendanceSession.builder()
                .maPDD(500L).maLop(1L).maHK(10L).ngay(today).phamVi("BUOI").maKhung("SANG").trangThai("DANG_NHAP").maTKTao(2L).build();
        when(attendanceDao.timPhienDiemDanh(1L, today, "BUOI", "SANG")).thenReturn(Optional.empty(), Optional.of(savedSession));
        when(attendanceDao.layChiTietDiemDanhTheoPhien(500L)).thenReturn(List.of(
                AttendanceStudentResponse.builder().maXL(101L).trangThai("CO_MAT").ghiChu("Ngoan").build()
        ));

        CreateAttendanceSessionRequest req = CreateAttendanceSessionRequest.builder()
                .maLop(1L)
                .ngay(today)
                .phamVi("BUOI")
                .maKhung("SANG")
                .chiTiet(List.of(AttendanceItemRequest.builder().maXL(101L).trangThai("CO_MAT").ghiChu("Ngoan").build()))
                .build();

        AttendanceSessionResponse response = attendanceService.taoPhienDiemDanh(req);

        assertNotNull(response);
        assertEquals(500L, response.getMaPDD());
        verify(attendanceDao, times(1)).taoPhienDiemDanh(any());
        verify(attendanceDao, times(1)).luuChiTietDiemDanh(anyList());
        verify(attendanceDao, times(1)).ghiNhatKy(eq(2L), eq("TAO_PHIEN_DIEM_DANH"), eq("PHIEN_DIEM_DANH"), eq("500"), isNull(), anyString(), any());
    }

    @Test
    @DisplayName("3. Cập nhật phiên điểm danh thành công")
    void testCapNhatPhienDiemDanh_ThanhCong() {
        when(nguoiDungHienTai.yeuCauVaiTro(any())).thenReturn(2L);
        when(userDao.findRoleNames(2L)).thenReturn(List.of("ADMIN"));

        AttendanceSession session = AttendanceSession.builder()
                .maPDD(500L).maLop(1L).ngay(today).phamVi("BUOI").maKhung("SANG").trangThai("DANG_NHAP").maTKTao(2L).build();
        when(attendanceDao.timPhienDiemDanhTheoId(500L)).thenReturn(Optional.of(session));
        when(schoolClassDao.findById(1L)).thenReturn(Optional.of(mockLop(1L, "10A1", "HOAT_DONG")));

        AttendanceRecord recCu = AttendanceRecord.builder().maDD(1L).maPDD(500L).maXL(101L).trangThai("CO_MAT").build();
        when(attendanceDao.layDiemDanhHienTaiTheoPhien(500L)).thenReturn(List.of(recCu));
        when(attendanceDao.timPhienDiemDanh(1L, today, "BUOI", "SANG")).thenReturn(Optional.of(session));

        UpdateAttendanceSessionRequest req = UpdateAttendanceSessionRequest.builder()
                .chiTiet(List.of(AttendanceItemRequest.builder().maXL(101L).trangThai("VANG_CO_PHEP").ghiChu("Xin phep om").build()))
                .build();

        AttendanceSessionResponse res = attendanceService.capNhatPhienDiemDanh(500L, req);

        assertNotNull(res);
        verify(attendanceDao, times(1)).capNhatChiTietDiemDanh(eq(500L), anyList());
        verify(attendanceDao, times(1)).ghiNhatKy(eq(2L), eq("CAP_NHAT_DIEM_DANH"), eq("PHIEN_DIEM_DANH"), eq("500"), anyString(), anyString(), any());
    }

    @Test
    @DisplayName("4. Từ chối khi giáo viên không được phân công chủ nhiệm hay bộ môn")
    void testGiaoVienKhongCoQuyen_TuChoi() {
        when(nguoiDungHienTai.yeuCauVaiTro(any(String[].class))).thenReturn(10L);
        when(userDao.findRoleNames(10L)).thenReturn(List.of("GIAO_VIEN"));
        when(attendanceDao.timMaGiaoVienTheoMaTK(10L)).thenReturn(Optional.of(99L));
        when(attendanceDao.laChuNhiemLop(99L, 1L, today)).thenReturn(false);
        when(attendanceDao.laGiaoVienBoMon(99L, 1L, null, today)).thenReturn(false);

        when(schoolClassDao.findById(1L)).thenReturn(Optional.of(mockLop(1L, "10A1", "HOAT_DONG")));

        assertThrows(AccessException.class, () -> attendanceService.layThongTinDiemDanh(1L, today, "BUOI", "SANG", null));
    }

    @Test
    @DisplayName("5. Từ chối khi ngày điểm danh trong tương lai")
    void testNgayDiemDanhTuongLai_TuChoi() {
        when(nguoiDungHienTai.yeuCauVaiTro(any())).thenReturn(2L);
        LocalDate tuongLai = today.plusDays(1);

        CreateAttendanceSessionRequest req = CreateAttendanceSessionRequest.builder()
                .maLop(1L).ngay(tuongLai).phamVi("BUOI").maKhung("SANG").chiTiet(List.of()).build();

        assertThrows(InvalidRequestException.class, () -> attendanceService.taoPhienDiemDanh(req));
    }

    @Test
    @DisplayName("6. Từ chối tạo phiên trùng lặp")
    void testTaoPhienDiemDanh_TrungLap_BaoLoi() {
        when(nguoiDungHienTai.yeuCauVaiTro(any())).thenReturn(2L);
        when(userDao.findRoleNames(2L)).thenReturn(List.of("GIAO_VU"));
        when(schoolClassDao.findById(1L)).thenReturn(Optional.of(mockLop(1L, "10A1", "HOAT_DONG")));

        AttendanceSession daCo = AttendanceSession.builder().maPDD(500L).build();
        when(attendanceDao.timPhienDiemDanh(1L, today, "BUOI", "SANG")).thenReturn(Optional.of(daCo));

        CreateAttendanceSessionRequest req = CreateAttendanceSessionRequest.builder()
                .maLop(1L).ngay(today).phamVi("BUOI").maKhung("SANG")
                .chiTiet(List.of(AttendanceItemRequest.builder().maXL(101L).trangThai("CO_MAT").build()))
                .build();

        assertThrows(BusinessException.class, () -> attendanceService.taoPhienDiemDanh(req));
    }

    @Test
    @DisplayName("7. Từ chối điểm danh học sinh không thuộc lớp tại ngày chọn")
    void testHocSinhKhongThuocLop_TuChoi() {
        when(nguoiDungHienTai.yeuCauVaiTro(any())).thenReturn(2L);
        when(userDao.findRoleNames(2L)).thenReturn(List.of("GIAO_VU"));
        when(schoolClassDao.findById(1L)).thenReturn(Optional.of(mockLop(1L, "10A1", "HOAT_DONG")));
        when(attendanceDao.timPhienDiemDanh(1L, today, "BUOI", "SANG")).thenReturn(Optional.empty());
        when(attendanceDao.timHocKyTheoNgayVaLop(1L, today)).thenReturn(Optional.of(10L));

        // Lớp chỉ có maXL 101L
        when(attendanceDao.layDanhSachHocSinhTheoLopVaNgay(1L, today)).thenReturn(List.of(
                AttendanceStudentResponse.builder().maXL(101L).build()
        ));

        // Request gửi maXL 999L không thuộc lớp
        CreateAttendanceSessionRequest req = CreateAttendanceSessionRequest.builder()
                .maLop(1L).ngay(today).phamVi("BUOI").maKhung("SANG")
                .chiTiet(List.of(AttendanceItemRequest.builder().maXL(999L).trangThai("CO_MAT").build()))
                .build();

        assertThrows(BusinessException.class, () -> attendanceService.taoPhienDiemDanh(req));
    }

    @Test
    @DisplayName("8. Từ chối khi ghi chú vượt quá 255 ký tự")
    void testGhiChuQua255KyTu_TuChoi() {
        when(nguoiDungHienTai.yeuCauVaiTro(any())).thenReturn(2L);
        when(userDao.findRoleNames(2L)).thenReturn(List.of("GIAO_VU"));
        when(schoolClassDao.findById(1L)).thenReturn(Optional.of(mockLop(1L, "10A1", "HOAT_DONG")));
        when(attendanceDao.timPhienDiemDanh(1L, today, "BUOI", "SANG")).thenReturn(Optional.empty());
        when(attendanceDao.timHocKyTheoNgayVaLop(1L, today)).thenReturn(Optional.of(10L));
        when(attendanceDao.layDanhSachHocSinhTheoLopVaNgay(1L, today)).thenReturn(List.of(
                AttendanceStudentResponse.builder().maXL(101L).build()
        ));

        String longNote = "A".repeat(256);
        CreateAttendanceSessionRequest req = CreateAttendanceSessionRequest.builder()
                .maLop(1L).ngay(today).phamVi("BUOI").maKhung("SANG")
                .chiTiet(List.of(AttendanceItemRequest.builder().maXL(101L).trangThai("CO_MAT").ghiChu(longNote).build()))
                .build();

        assertThrows(InvalidRequestException.class, () -> attendanceService.taoPhienDiemDanh(req));
    }

    @Test
    @DisplayName("9. Không cho phép chỉnh sửa phiên đã chốt sổ (khóa)")
    void testPhienDaChot_KhongDuocSua() {
        when(nguoiDungHienTai.yeuCauVaiTro(any())).thenReturn(2L);

        AttendanceSession phienChot = AttendanceSession.builder()
                .maPDD(500L).maLop(1L).trangThai("DA_CHOT").build();
        when(attendanceDao.timPhienDiemDanhTheoId(500L)).thenReturn(Optional.of(phienChot));

        UpdateAttendanceSessionRequest req = UpdateAttendanceSessionRequest.builder()
                .chiTiet(List.of(AttendanceItemRequest.builder().maXL(101L).trangThai("CO_MAT").build()))
                .build();

        assertThrows(BusinessException.class, () -> attendanceService.capNhatPhienDiemDanh(500L, req));
    }

    @Test
    @DisplayName("10. Từ chối khi lớp không có học sinh tại ngày điểm danh")
    void testLopKhongCoHocSinh_TuChoi() {
        when(nguoiDungHienTai.yeuCauVaiTro(any())).thenReturn(2L);
        when(userDao.findRoleNames(2L)).thenReturn(List.of("GIAO_VU"));
        when(schoolClassDao.findById(1L)).thenReturn(Optional.of(mockLop(1L, "10A1", "HOAT_DONG")));
        when(attendanceDao.timPhienDiemDanh(1L, today, "BUOI", "SANG")).thenReturn(Optional.empty());
        when(attendanceDao.timHocKyTheoNgayVaLop(1L, today)).thenReturn(Optional.of(10L));
        when(attendanceDao.layDanhSachHocSinhTheoLopVaNgay(1L, today)).thenReturn(List.of());

        CreateAttendanceSessionRequest req = CreateAttendanceSessionRequest.builder()
                .maLop(1L).ngay(today).phamVi("BUOI").maKhung("SANG")
                .chiTiet(List.of(AttendanceItemRequest.builder().maXL(101L).trangThai("CO_MAT").build()))
                .build();

        assertThrows(BusinessException.class, () -> attendanceService.taoPhienDiemDanh(req));
    }
}
