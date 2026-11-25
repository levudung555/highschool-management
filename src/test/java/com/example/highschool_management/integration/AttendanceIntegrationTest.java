package com.example.highschool_management.integration;

import com.example.highschool_management.dto.request.AttendanceItemRequest;
import com.example.highschool_management.dto.request.CreateAttendanceSessionRequest;
import com.example.highschool_management.dto.request.UpdateAttendanceSessionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
    properties = {
      "app.seed.enabled=false",
      "spring.datasource.url=jdbc:h2:mem:attendance;MODE=MySQL;DB_CLOSE_DELAY=-1",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "spring.datasource.username=sa",
      "spring.datasource.password=",
      "spring.sql.init.mode=never"
    })
@AutoConfigureMockMvc
@Sql("/uc09-schema.sql")
class AttendanceIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private ObjectMapper json;

    private String token;

    @BeforeEach
    void initDatabase() throws Exception {
        // Tạo thêm bảng liên quan đến điểm danh trong H2 nếu chưa có
        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS hoc_ky (
                MaHK BIGINT PRIMARY KEY,
                MaNH BIGINT,
                SoHocKy TINYINT,
                NgayBatDau DATE,
                NgayKetThuc DATE,
                TrangThai VARCHAR(20)
            );
        """);

        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS phien_diem_danh (
                MaPDD BIGINT AUTO_INCREMENT PRIMARY KEY,
                MaLop BIGINT NOT NULL,
                MaHK BIGINT NOT NULL,
                MaLMH BIGINT NULL,
                Ngay DATE NOT NULL,
                PhamVi VARCHAR(10) NOT NULL,
                MaKhung VARCHAR(30) NOT NULL,
                TrangThai VARCHAR(20) DEFAULT 'DANG_NHAP',
                MaTKTao BIGINT NOT NULL,
                CONSTRAINT uk_pdd_lop_ngay_khung UNIQUE (MaLop, Ngay, PhamVi, MaKhung)
            );
        """);

        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS diem_danh (
                MaDD BIGINT AUTO_INCREMENT PRIMARY KEY,
                MaPDD BIGINT NOT NULL,
                MaXL BIGINT NOT NULL,
                TrangThai VARCHAR(25) DEFAULT 'CHUA_GHI_NHAN',
                SoPhutMuon SMALLINT DEFAULT 0,
                GhiChu VARCHAR(500) NULL,
                MaTKCapNhat BIGINT NOT NULL,
                CapNhatLuc TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                CONSTRAINT uk_dd_phien_xeplop UNIQUE (MaPDD, MaXL)
            );
        """);

        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS nhat_ky_he_thong (
                MaNK BIGINT AUTO_INCREMENT PRIMARY KEY,
                MaTK BIGINT NOT NULL,
                HanhDong VARCHAR(60) NOT NULL,
                LoaiDoiTuong VARCHAR(60) NULL,
                MaDoiTuong VARCHAR(100) NULL,
                DuLieuTruoc VARCHAR(4000) NULL,
                DuLieuSau VARCHAR(4000) NULL,
                ThoiDiem TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        """);

        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS giao_vien (
                MaGV BIGINT AUTO_INCREMENT PRIMARY KEY,
                MaSoGV VARCHAR(30) NOT NULL UNIQUE,
                HoTen VARCHAR(150) NOT NULL,
                NgaySinh DATE NULL,
                SoDienThoai VARCHAR(20) NULL,
                Email VARCHAR(254) NULL,
                ChuyenMon VARCHAR(150) NULL,
                TrangThai VARCHAR(25) DEFAULT 'DANG_CONG_TAC'
            );
        """);

        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS phan_cong_chu_nhiem (
                MaPCCN BIGINT AUTO_INCREMENT PRIMARY KEY,
                MaLop BIGINT NOT NULL,
                MaGV BIGINT NOT NULL,
                TuNgay DATE NOT NULL,
                DenNgay DATE NULL,
                TrangThaiDuyet VARCHAR(20) DEFAULT 'CHO_DUYET',
                MaTKDuyet BIGINT NULL,
                DuyetLuc TIMESTAMP NULL
            );
        """);

        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS lop_mon_hoc (
                MaLMH BIGINT AUTO_INCREMENT PRIMARY KEY,
                MaLop BIGINT NOT NULL,
                MaMH BIGINT NOT NULL,
                MaHK BIGINT NOT NULL,
                HinhThucDanhGia VARCHAR(30) NOT NULL,
                SoTietNam SMALLINT NULL
            );
        """);

        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS phan_cong_giang_day (
                MaPCGD BIGINT AUTO_INCREMENT PRIMARY KEY,
                MaLMH BIGINT NOT NULL,
                MaGV BIGINT NOT NULL,
                TuNgay DATE NOT NULL,
                DenNgay DATE NULL,
                TrangThaiDuyet VARCHAR(20) DEFAULT 'CHO_DUYET',
                MaTKDuyet BIGINT NULL,
                DuyetLuc TIMESTAMP NULL
            );
        """);

        // Dữ liệu học kỳ
        jdbc.execute("DELETE FROM hoc_ky;");
        jdbc.execute("INSERT INTO hoc_ky (MaHK, MaNH, SoHocKy, NgayBatDau, NgayKetThuc, TrangThai) VALUES (1, 1, 1, '2023-09-05', '2024-01-15', 'DA_KET_THUC');");
        jdbc.execute("INSERT INTO hoc_ky (MaHK, MaNH, SoHocKy, NgayBatDau, NgayKetThuc, TrangThai) VALUES (2, 1, 2, '2024-01-16', '2024-05-31', 'DA_KET_THUC');");

        // Clear phiên & điểm danh cũ
        jdbc.execute("DELETE FROM diem_danh;");
        jdbc.execute("DELETE FROM phien_diem_danh;");
        jdbc.execute("DELETE FROM nhat_ky_he_thong;");

        // Login với user giaovu (vai trò GIAO_VU)
        var result = mvc.perform(post("/auth/token")
                .contentType("application/json")
                .content("{\"username\":\"giaovu\",\"password\":\"Test@12345\"}"))
                .andExpect(status().isOk())
                .andReturn();
        token = com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.result.token");
    }

    @Test
    @DisplayName("API lấy danh sách lớp được phép điểm danh")
    void testLayDanhSachLop() throws Exception {
        mvc.perform(get("/api/v1/attendance/classes")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].maLop").value(1))
                .andExpect(jsonPath("$[0].tenLop").value("10A1"));
    }

    @Test
    @DisplayName("API lấy thông tin điểm danh - Chưa có phiên, trả về học sinh mặc định CO_MAT")
    void testLayThongTinDiemDanh_ChuaCoPhien() throws Exception {
        mvc.perform(get("/api/v1/attendance/session")
                .header("Authorization", "Bearer " + token)
                .param("maLop", "1")
                .param("ngay", "2023-10-15")
                .param("phamVi", "BUOI")
                .param("maKhung", "SANG"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trangThai").value("CHUA_TAO"))
                .andExpect(jsonPath("$.danhSachHocSinh").isArray())
                .andExpect(jsonPath("$.danhSachHocSinh[0].maXL").value(1))
                .andExpect(jsonPath("$.danhSachHocSinh[0].trangThai").value("CO_MAT"));
    }

    @Test
    @DisplayName("API tạo phiên điểm danh và cập nhật phiên thành công")
    void testTaoVaCapNhatPhienDiemDanh() throws Exception {
        CreateAttendanceSessionRequest createReq = CreateAttendanceSessionRequest.builder()
                .maLop(1L)
                .maHK(1L)
                .ngay(LocalDate.of(2023, 10, 15))
                .phamVi("BUOI")
                .maKhung("SANG")
                .chiTiet(List.of(
                        AttendanceItemRequest.builder().maXL(1L).trangThai("CO_MAT").ghiChu("Dung gio").build()
                ))
                .build();

        var createResult = mvc.perform(post("/api/v1/attendance/session")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(json.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.maPDD").isNumber())
                .andExpect(jsonPath("$.trangThai").value("DANG_NHAP"))
                .andExpect(jsonPath("$.danhSachHocSinh[0].trangThai").value("CO_MAT"))
                .andReturn();

        Number maPDDNum = com.jayway.jsonpath.JsonPath.read(createResult.getResponse().getContentAsString(), "$.maPDD");
        Long maPDD = maPDDNum.longValue();

        // Kiểm tra database đã có bản ghi
        Integer pddCount = jdbc.queryForObject("SELECT COUNT(*) FROM phien_diem_danh WHERE MaPDD = ?", Integer.class, maPDD);
        assertEquals(1, pddCount);
        Integer ddCount = jdbc.queryForObject("SELECT COUNT(*) FROM diem_danh WHERE MaPDD = ?", Integer.class, maPDD);
        assertEquals(1, ddCount);

        // Cập nhật trạng thái sang VANG_CO_PHEP
        UpdateAttendanceSessionRequest updateReq = UpdateAttendanceSessionRequest.builder()
                .chiTiet(List.of(
                        AttendanceItemRequest.builder().maXL(1L).trangThai("VANG_CO_PHEP").ghiChu("Phep om").build()
                ))
                .build();

        mvc.perform(put("/api/v1/attendance/session/" + maPDD)
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(json.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.danhSachHocSinh[0].trangThai").value("VANG_CO_PHEP"))
                .andExpect(jsonPath("$.danhSachHocSinh[0].ghiChu").value("Phep om"));

        // Kiểm tra nhat_ky_he_thong
        Integer logCount = jdbc.queryForObject("SELECT COUNT(*) FROM nhat_ky_he_thong WHERE LoaiDoiTuong = 'PHIEN_DIEM_DANH'", Integer.class);
        assertEquals(2, logCount); // 1 tao + 1 cap nhat
    }

    @Test
    @DisplayName("API tạo phiên trùng lặp - Trả về lỗi 409 Conflict")
    void testTaoPhienTrungLap_Loi409() throws Exception {
        CreateAttendanceSessionRequest createReq = CreateAttendanceSessionRequest.builder()
                .maLop(1L)
                .maHK(1L)
                .ngay(LocalDate.of(2023, 10, 16))
                .phamVi("BUOI")
                .maKhung("SANG")
                .chiTiet(List.of(
                        AttendanceItemRequest.builder().maXL(1L).trangThai("CO_MAT").ghiChu("").build()
                ))
                .build();

        mvc.perform(post("/api/v1/attendance/session")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(json.writeValueAsString(createReq)))
                .andExpect(status().isCreated());

        // Lần thứ hai cùng ngày, lớp, buổi
        mvc.perform(post("/api/v1/attendance/session")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(json.writeValueAsString(createReq)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("đã tồn tại")));
    }
}
