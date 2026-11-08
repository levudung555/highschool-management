-- HE THONG QUAN LY HOC SINH THPT - ERD F01-F20
-- MySQL >= 8.0.16; InnoDB; utf8mb4. Generated: 2026-11-08.
-- FRESH INSTALL ONLY. No DROP/TRUNCATE; no existing data is replaced.
-- Default isolated database. Change BOTH database names below if necessary.
-- Stop on the first SQL error; do not import with mysql --force.
-- Re-running against an existing schema intentionally fails on CREATE TABLE.
-- XEP_LOP uses inclusive [TuNgay, DenNgay], direct operations, no approval.
-- Other approval workflows and their original time conventions are preserved.
-- Cross-table rules, authorization, locks and atomic transfers belong to service.
SET NAMES utf8mb4 COLLATE utf8mb4_0900_ai_ci;
SET SESSION time_zone = '+07:00';
CREATE DATABASE IF NOT EXISTS `highschool_management_v2`
  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `highschool_management_v2`;

-- 01. GIAO_VIEN
CREATE TABLE `giao_vien` (
  `MaGV` bigint unsigned NOT NULL AUTO_INCREMENT,
  `MaSoGV` varchar(30) NOT NULL,
  `HoTen` varchar(150) NOT NULL,
  `NgaySinh` date DEFAULT NULL,
  `SoDienThoai` varchar(20) DEFAULT NULL,
  `Email` varchar(254) DEFAULT NULL,
  `ChuyenMon` varchar(150) DEFAULT NULL,
  `TrangThai` varchar(25) NOT NULL DEFAULT 'DANG_CONG_TAC',
  PRIMARY KEY (`MaGV`),
  UNIQUE KEY `uk_gv_maso` (`MaSoGV`),
  KEY `ix_GIAO_VIEN_HoTen` (`HoTen`),
  CONSTRAINT `ck_gv_hoten` CHECK ((char_length(trim(`HoTen`)) > 0)),
  CONSTRAINT `ck_gv_maso` CHECK ((char_length(trim(`MaSoGV`)) > 0)),
  CONSTRAINT `ck_gv_trangthai` CHECK ((`TrangThai` in ('DANG_CONG_TAC','NGUNG_CONG_TAC')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 02. HOC_SINH
CREATE TABLE `hoc_sinh` (
  `MaHS` bigint unsigned NOT NULL AUTO_INCREMENT,
  `MaSoHS` varchar(30) NOT NULL,
  `HoTen` varchar(150) NOT NULL,
  `NgaySinh` date NOT NULL,
  `GioiTinh` varchar(10) NOT NULL,
  `DiaChi` varchar(500) DEFAULT NULL,
  `SoDienThoai` varchar(20) DEFAULT NULL,
  `Email` varchar(254) DEFAULT NULL,
  `NgayTiepNhan` date DEFAULT NULL,
  PRIMARY KEY (`MaHS`),
  UNIQUE KEY `uk_hs_maso` (`MaSoHS`),
  KEY `ix_HOC_SINH_HoTen` (`HoTen`),
  CONSTRAINT `ck_hs_gioitinh` CHECK ((`GioiTinh` in ('NAM','NU','KHAC'))),
  CONSTRAINT `ck_hs_hoten` CHECK ((char_length(trim(`HoTen`)) > 0)),
  CONSTRAINT `ck_hs_maso` CHECK ((char_length(trim(`MaSoHS`)) > 0)),
  CONSTRAINT `ck_hs_tiepnhan` CHECK (((`NgayTiepNhan` is null) or (`NgayTiepNhan` >= `NgaySinh`)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 03. KHOI_LOP
CREATE TABLE `khoi_lop` (
  `MaKhoi` bigint unsigned NOT NULL AUTO_INCREMENT,
  `SoKhoi` tinyint unsigned NOT NULL,
  PRIMARY KEY (`MaKhoi`),
  UNIQUE KEY `uk_khoi_so` (`SoKhoi`),
  CONSTRAINT `ck_khoi_so` CHECK ((`SoKhoi` in (10,11,12)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 04. MON_HOC
CREATE TABLE `mon_hoc` (
  `MaMH` bigint unsigned NOT NULL AUTO_INCREMENT,
  `MaSoMon` varchar(30) NOT NULL,
  `TenMon` varchar(150) NOT NULL,
  PRIMARY KEY (`MaMH`),
  UNIQUE KEY `uk_mon_maso` (`MaSoMon`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 05. NAM_HOC
CREATE TABLE `nam_hoc` (
  `MaNH` bigint unsigned NOT NULL AUTO_INCREMENT,
  `MaSoNH` varchar(20) NOT NULL,
  `NgayBatDau` date NOT NULL,
  `NgayKetThuc` date NOT NULL,
  PRIMARY KEY (`MaNH`),
  UNIQUE KEY `uk_nh_maso` (`MaSoNH`),
  CONSTRAINT `ck_nh_ngay` CHECK ((`NgayKetThuc` > `NgayBatDau`))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 06. NGUOI_GIAM_HO
CREATE TABLE `nguoi_giam_ho` (
  `MaNGH` bigint unsigned NOT NULL AUTO_INCREMENT,
  `HoTen` varchar(150) NOT NULL,
  `SoDienThoai` varchar(20) DEFAULT NULL,
  `Email` varchar(254) DEFAULT NULL,
  `DiaChi` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`MaNGH`),
  KEY `ix_NGUOI_GIAM_HO_SoDienThoai` (`SoDienThoai`),
  CONSTRAINT `ck_ngh_hoten` CHECK ((char_length(trim(`HoTen`)) > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 07. VAI_TRO
CREATE TABLE `vai_tro` (
  `MaVT` bigint unsigned NOT NULL AUTO_INCREMENT,
  `MaSoVaiTro` varchar(40) NOT NULL,
  `TenVaiTro` varchar(100) NOT NULL,
  PRIMARY KEY (`MaVT`),
  UNIQUE KEY `uk_vt_maso` (`MaSoVaiTro`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 08. HOC_KY
CREATE TABLE `hoc_ky` (
  `MaHK` bigint unsigned NOT NULL AUTO_INCREMENT,
  `MaNH` bigint unsigned NOT NULL,
  `SoHocKy` tinyint unsigned NOT NULL,
  `NgayBatDau` date NOT NULL,
  `NgayKetThuc` date NOT NULL,
  `TrangThai` varchar(20) NOT NULL DEFAULT 'CHUA_BAT_DAU',
  PRIMARY KEY (`MaHK`),
  UNIQUE KEY `uk_hk_nam_so` (`MaNH`,`SoHocKy`),
  CONSTRAINT `fk_HOC_KY_MaNH` FOREIGN KEY (`MaNH`) REFERENCES `nam_hoc` (`MaNH`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `ck_hk_ngay` CHECK ((`NgayKetThuc` > `NgayBatDau`)),
  CONSTRAINT `ck_hk_so` CHECK ((`SoHocKy` in (1,2))),
  CONSTRAINT `ck_hk_trangthai` CHECK ((`TrangThai` in ('CHUA_BAT_DAU','DANG_DIEN_RA','DA_KET_THUC')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 09. HS_NGUOI_GIAM_HO
CREATE TABLE `hs_nguoi_giam_ho` (
  `MaHS` bigint unsigned NOT NULL,
  `MaNGH` bigint unsigned NOT NULL,
  `MoiQuanHe` varchar(50) NOT NULL,
  `LienHeChinh` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`MaHS`,`MaNGH`),
  KEY `fk_HS_NGUOI_GIAM_HO_MaNGH` (`MaNGH`),
  CONSTRAINT `fk_HS_NGUOI_GIAM_HO_MaHS` FOREIGN KEY (`MaHS`) REFERENCES `hoc_sinh` (`MaHS`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_HS_NGUOI_GIAM_HO_MaNGH` FOREIGN KEY (`MaNGH`) REFERENCES `nguoi_giam_ho` (`MaNGH`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `ck_hsngh_chinh` CHECK ((`LienHeChinh` in (0,1)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 10. LOP_HOC
CREATE TABLE `lop_hoc` (
  `MaLop` bigint unsigned NOT NULL AUTO_INCREMENT,
  `MaNH` bigint unsigned NOT NULL,
  `MaKhoi` bigint unsigned NOT NULL,
  `TenLop` varchar(30) NOT NULL,
  `TrangThai` varchar(20) NOT NULL DEFAULT 'HOAT_DONG',
  PRIMARY KEY (`MaLop`),
  UNIQUE KEY `uk_lop_nam_ten` (`MaNH`,`TenLop`),
  KEY `fk_LOP_HOC_MaKhoi` (`MaKhoi`),
  CONSTRAINT `fk_LOP_HOC_MaKhoi` FOREIGN KEY (`MaKhoi`) REFERENCES `khoi_lop` (`MaKhoi`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_LOP_HOC_MaNH` FOREIGN KEY (`MaNH`) REFERENCES `nam_hoc` (`MaNH`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `ck_lop_ten` CHECK ((char_length(trim(`TenLop`)) > 0)),
  CONSTRAINT `ck_lop_trangthai` CHECK ((`TrangThai` in ('HOAT_DONG','NGUNG_HOAT_DONG')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 11. TAI_KHOAN
CREATE TABLE `tai_khoan` (
  `MaTK` bigint unsigned NOT NULL AUTO_INCREMENT,
  `MaGV` bigint unsigned DEFAULT NULL,
  `TenDangNhap` varchar(100) NOT NULL,
  `MatKhauBam` varchar(255) NOT NULL,
  `HoTen` varchar(150) NOT NULL,
  `Email` varchar(254) DEFAULT NULL,
  `TrangThai` varchar(20) NOT NULL DEFAULT 'HOAT_DONG',
  `TokenVersion` int unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`MaTK`),
  UNIQUE KEY `uk_tk_tendangnhap` (`TenDangNhap`),
  UNIQUE KEY `uk_tk_gv` (`MaGV`),
  CONSTRAINT `fk_TAI_KHOAN_MaGV` FOREIGN KEY (`MaGV`) REFERENCES `giao_vien` (`MaGV`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `ck_tk_matkhau` CHECK ((char_length(trim(`MatKhauBam`)) > 0)),
  CONSTRAINT `ck_tk_tendangnhap` CHECK ((char_length(trim(`TenDangNhap`)) > 0)),
  CONSTRAINT `ck_tk_trangthai` CHECK ((`TrangThai` in ('HOAT_DONG','BI_KHOA')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 12. KET_QUA_MON
CREATE TABLE `ket_qua_mon` (
  `MaKQM` bigint unsigned NOT NULL AUTO_INCREMENT,
  `MaHS` bigint unsigned NOT NULL,
  `MaMH` bigint unsigned NOT NULL,
  `MaHK` bigint unsigned NOT NULL,
  `HinhThucDanhGia` varchar(30) NOT NULL,
  `TrangThai` varchar(20) NOT NULL DEFAULT 'DANG_HOC',
  `LyDoMien` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`MaKQM`),
  UNIQUE KEY `uk_kqm_hs_mon_ky` (`MaHS`,`MaMH`,`MaHK`),
  KEY `fk_KET_QUA_MON_MaMH` (`MaMH`),
  KEY `fk_KET_QUA_MON_MaHK` (`MaHK`),
  CONSTRAINT `fk_KET_QUA_MON_MaHK` FOREIGN KEY (`MaHK`) REFERENCES `hoc_ky` (`MaHK`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_KET_QUA_MON_MaHS` FOREIGN KEY (`MaHS`) REFERENCES `hoc_sinh` (`MaHS`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_KET_QUA_MON_MaMH` FOREIGN KEY (`MaMH`) REFERENCES `mon_hoc` (`MaMH`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `ck_kqm_hinhthuc` CHECK ((`HinhThucDanhGia` in ('NHAN_XET','DIEM_SO_KEM_NHAN_XET'))),
  CONSTRAINT `ck_kqm_mien` CHECK (((`TrangThai` <> 'MIEN_DANH_GIA') or ((`LyDoMien` is not null) and (char_length(trim(`LyDoMien`)) > 0)))),
  CONSTRAINT `ck_kqm_trangthai` CHECK ((`TrangThai` in ('DANG_HOC','DA_CHOT','MIEN_DANH_GIA')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 13. LOP_MON_HOC
CREATE TABLE `lop_mon_hoc` (
  `MaLMH` bigint unsigned NOT NULL AUTO_INCREMENT,
  `MaLop` bigint unsigned NOT NULL,
  `MaMH` bigint unsigned NOT NULL,
  `MaHK` bigint unsigned NOT NULL,
  `HinhThucDanhGia` varchar(30) NOT NULL,
  `SoTietNam` smallint unsigned DEFAULT NULL,
  PRIMARY KEY (`MaLMH`),
  UNIQUE KEY `uk_lmh_lop_mon_ky` (`MaLop`,`MaMH`,`MaHK`),
  KEY `fk_LOP_MON_HOC_MaMH` (`MaMH`),
  KEY `fk_LOP_MON_HOC_MaHK` (`MaHK`),
  CONSTRAINT `fk_LOP_MON_HOC_MaHK` FOREIGN KEY (`MaHK`) REFERENCES `hoc_ky` (`MaHK`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_LOP_MON_HOC_MaLop` FOREIGN KEY (`MaLop`) REFERENCES `lop_hoc` (`MaLop`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_LOP_MON_HOC_MaMH` FOREIGN KEY (`MaMH`) REFERENCES `mon_hoc` (`MaMH`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `ck_lmh_hinhthuc` CHECK ((`HinhThucDanhGia` in ('NHAN_XET','DIEM_SO_KEM_NHAN_XET'))),
  CONSTRAINT `ck_lmh_sotiet` CHECK (((`SoTietNam` is null) or (`SoTietNam` > 0)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 14. NHAT_KY_HE_THONG
CREATE TABLE `nhat_ky_he_thong` (
  `MaNK` bigint unsigned NOT NULL AUTO_INCREMENT,
  `MaTK` bigint unsigned NOT NULL,
  `HanhDong` varchar(60) NOT NULL,
  `LoaiDoiTuong` varchar(60) DEFAULT NULL,
  `MaDoiTuong` varchar(100) DEFAULT NULL,
  `DuLieuTruoc` json DEFAULT NULL,
  `DuLieuSau` json DEFAULT NULL,
  `ThoiDiem` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`MaNK`),
  KEY `ix_NHAT_KY_HE_THONG_MaTK_ThoiDiem` (`MaTK`,`ThoiDiem`),
  KEY `ix_NHAT_KY_HE_THONG_LoaiDoiTuong_MaDoiTuong` (`LoaiDoiTuong`,`MaDoiTuong`),
  KEY `ix_NHAT_KY_HE_THONG_ThoiDiem` (`ThoiDiem`),
  CONSTRAINT `fk_NHAT_KY_HE_THONG_MaTK` FOREIGN KEY (`MaTK`) REFERENCES `tai_khoan` (`MaTK`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 15. PHAN_CONG_CHU_NHIEM
CREATE TABLE `phan_cong_chu_nhiem` (
  `MaPCCN` bigint unsigned NOT NULL AUTO_INCREMENT,
  `MaLop` bigint unsigned NOT NULL,
  `MaGV` bigint unsigned NOT NULL,
  `TuNgay` date NOT NULL,
  `DenNgay` date DEFAULT NULL,
  `TrangThaiDuyet` varchar(20) NOT NULL DEFAULT 'CHO_DUYET',
  `MaTKDuyet` bigint unsigned DEFAULT NULL,
  `DuyetLuc` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`MaPCCN`),
  KEY `ix_PHAN_CONG_CHU_NHIEM_MaLop_TrangThaiDuyet_TuNgay_DenNgay` (`MaLop`,`TrangThaiDuyet`,`TuNgay`,`DenNgay`),
  KEY `ix_PHAN_CONG_CHU_NHIEM_MaGV_TuNgay` (`MaGV`,`TuNgay`),
  KEY `fk_PHAN_CONG_CHU_NHIEM_MaTKDuyet` (`MaTKDuyet`),
  CONSTRAINT `fk_PHAN_CONG_CHU_NHIEM_MaGV` FOREIGN KEY (`MaGV`) REFERENCES `giao_vien` (`MaGV`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_PHAN_CONG_CHU_NHIEM_MaLop` FOREIGN KEY (`MaLop`) REFERENCES `lop_hoc` (`MaLop`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_PHAN_CONG_CHU_NHIEM_MaTKDuyet` FOREIGN KEY (`MaTKDuyet`) REFERENCES `tai_khoan` (`MaTK`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `ck_MaPCCN_duyet` CHECK ((`TrangThaiDuyet` in ('CHO_DUYET','DA_DUYET','TU_CHOI'))),
  CONSTRAINT `ck_MaPCCN_nguoiduyet` CHECK ((((`TrangThaiDuyet` = 'CHO_DUYET') and (`MaTKDuyet` is null) and (`DuyetLuc` is null)) or ((`TrangThaiDuyet` in ('DA_DUYET','TU_CHOI')) and (`MaTKDuyet` is not null) and (`DuyetLuc` is not null)))),
  CONSTRAINT `ck_MaPCCN_thoigian` CHECK (((`DenNgay` is null) or (`DenNgay` > `TuNgay`)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 16. TAI_KHOAN_VAI_TRO
CREATE TABLE `tai_khoan_vai_tro` (
  `MaTK` bigint unsigned NOT NULL,
  `MaVT` bigint unsigned NOT NULL,
  PRIMARY KEY (`MaTK`,`MaVT`),
  KEY `fk_TAI_KHOAN_VAI_TRO_MaVT` (`MaVT`),
  CONSTRAINT `fk_TAI_KHOAN_VAI_TRO_MaTK` FOREIGN KEY (`MaTK`) REFERENCES `tai_khoan` (`MaTK`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_TAI_KHOAN_VAI_TRO_MaVT` FOREIGN KEY (`MaVT`) REFERENCES `vai_tro` (`MaVT`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 17. TONG_KET_HOC_KY
CREATE TABLE `tong_ket_hoc_ky` (
  `MaTKHocKy` bigint unsigned NOT NULL AUTO_INCREMENT,
  `MaHS` bigint unsigned NOT NULL,
  `MaHK` bigint unsigned NOT NULL,
  `KetQuaHocTap` varchar(20) DEFAULT NULL,
  `KetQuaRenLuyen` varchar(20) DEFAULT NULL,
  `NhanXet` text,
  `TrangThaiDuyet` varchar(20) NOT NULL DEFAULT 'CHO_DUYET',
  `MaTKDuyet` bigint unsigned DEFAULT NULL,
  `DuyetLuc` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`MaTKHocKy`),
  UNIQUE KEY `uk_MaTKHocKy_hs_ky` (`MaHS`,`MaHK`),
  KEY `fk_TONG_KET_HOC_KY_MaHK` (`MaHK`),
  KEY `fk_TONG_KET_HOC_KY_MaTKDuyet` (`MaTKDuyet`),
  CONSTRAINT `fk_TONG_KET_HOC_KY_MaHK` FOREIGN KEY (`MaHK`) REFERENCES `hoc_ky` (`MaHK`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_TONG_KET_HOC_KY_MaHS` FOREIGN KEY (`MaHS`) REFERENCES `hoc_sinh` (`MaHS`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_TONG_KET_HOC_KY_MaTKDuyet` FOREIGN KEY (`MaTKDuyet`) REFERENCES `tai_khoan` (`MaTK`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `ck_MaTKHocKy_daydu` CHECK (((`TrangThaiDuyet` <> 'DA_DUYET') or ((`KetQuaHocTap` is not null) and (`KetQuaRenLuyen` is not null)))),
  CONSTRAINT `ck_MaTKHocKy_duyet` CHECK ((`TrangThaiDuyet` in ('CHO_DUYET','DA_DUYET','TU_CHOI'))),
  CONSTRAINT `ck_MaTKHocKy_hoctap` CHECK (((`KetQuaHocTap` is null) or (`KetQuaHocTap` in ('TOT','KHA','DAT','CHUA_DAT')))),
  CONSTRAINT `ck_MaTKHocKy_nguoiduyet` CHECK ((((`TrangThaiDuyet` = 'CHO_DUYET') and (`MaTKDuyet` is null) and (`DuyetLuc` is null)) or ((`TrangThaiDuyet` in ('DA_DUYET','TU_CHOI')) and (`MaTKDuyet` is not null) and (`DuyetLuc` is not null)))),
  CONSTRAINT `ck_MaTKHocKy_renluyen` CHECK (((`KetQuaRenLuyen` is null) or (`KetQuaRenLuyen` in ('TOT','KHA','DAT','CHUA_DAT'))))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 18. TONG_KET_NAM
CREATE TABLE `tong_ket_nam` (
  `MaTKNam` bigint unsigned NOT NULL AUTO_INCREMENT,
  `MaHS` bigint unsigned NOT NULL,
  `MaNH` bigint unsigned NOT NULL,
  `KetQuaHocTap` varchar(20) DEFAULT NULL,
  `KetQuaRenLuyen` varchar(20) DEFAULT NULL,
  `KetQuaLenLop` varchar(30) DEFAULT NULL,
  `TrangThaiDuyet` varchar(20) NOT NULL DEFAULT 'CHO_DUYET',
  `MaTKDuyet` bigint unsigned DEFAULT NULL,
  `DuyetLuc` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`MaTKNam`),
  UNIQUE KEY `uk_MaTKNam_hs_ky` (`MaHS`,`MaNH`),
  KEY `fk_TONG_KET_NAM_MaNH` (`MaNH`),
  KEY `fk_TONG_KET_NAM_MaTKDuyet` (`MaTKDuyet`),
  CONSTRAINT `fk_TONG_KET_NAM_MaHS` FOREIGN KEY (`MaHS`) REFERENCES `hoc_sinh` (`MaHS`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_TONG_KET_NAM_MaNH` FOREIGN KEY (`MaNH`) REFERENCES `nam_hoc` (`MaNH`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_TONG_KET_NAM_MaTKDuyet` FOREIGN KEY (`MaTKDuyet`) REFERENCES `tai_khoan` (`MaTK`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `ck_MaTKNam_daydu` CHECK (((`TrangThaiDuyet` <> 'DA_DUYET') or ((`KetQuaHocTap` is not null) and (`KetQuaRenLuyen` is not null)))),
  CONSTRAINT `ck_MaTKNam_duyet` CHECK ((`TrangThaiDuyet` in ('CHO_DUYET','DA_DUYET','TU_CHOI'))),
  CONSTRAINT `ck_MaTKNam_hoctap` CHECK (((`KetQuaHocTap` is null) or (`KetQuaHocTap` in ('TOT','KHA','DAT','CHUA_DAT')))),
  CONSTRAINT `ck_MaTKNam_nguoiduyet` CHECK ((((`TrangThaiDuyet` = 'CHO_DUYET') and (`MaTKDuyet` is null) and (`DuyetLuc` is null)) or ((`TrangThaiDuyet` in ('DA_DUYET','TU_CHOI')) and (`MaTKDuyet` is not null) and (`DuyetLuc` is not null)))),
  CONSTRAINT `ck_MaTKNam_renluyen` CHECK (((`KetQuaRenLuyen` is null) or (`KetQuaRenLuyen` in ('TOT','KHA','DAT','CHUA_DAT')))),
  CONSTRAINT `ck_tkn_lenlop` CHECK (((`KetQuaLenLop` is null) or (`KetQuaLenLop` in ('LEN_LOP','KHONG_LEN_LOP','CHO_DANH_GIA_LAI','KHONG_AP_DUNG'))))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 19. XEP_LOP
CREATE TABLE `xep_lop` (
  `MaXL` bigint unsigned NOT NULL AUTO_INCREMENT,
  `MaHS` bigint unsigned NOT NULL,
  `MaLop` bigint unsigned NOT NULL,
  `TuNgay` date NOT NULL COMMENT 'Ngay dau tien thuoc lop (bao gom)',
  `DenNgay` date DEFAULT NULL COMMENT 'Ngay cuoi cung thuoc lop (bao gom); NULL = chua ket thuc',
  `LyDoKetThuc` varchar(255) DEFAULT NULL,
  `MaTKNguoiThucHien` bigint unsigned NOT NULL,
  `ThoiDiemThucHien` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`MaXL`),
  KEY `ix_XEP_LOP_MaHS_TuNgay_DenNgay` (`MaHS`,`TuNgay`,`DenNgay`),
  KEY `ix_XEP_LOP_MaLop_TuNgay_DenNgay` (`MaLop`,`TuNgay`,`DenNgay`),
  KEY `fk_XEP_LOP_MaTKNguoiThucHien` (`MaTKNguoiThucHien`),
  CONSTRAINT `fk_XEP_LOP_MaHS` FOREIGN KEY (`MaHS`) REFERENCES `hoc_sinh` (`MaHS`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_XEP_LOP_MaLop` FOREIGN KEY (`MaLop`) REFERENCES `lop_hoc` (`MaLop`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_XEP_LOP_MaTKNguoiThucHien` FOREIGN KEY (`MaTKNguoiThucHien`) REFERENCES `tai_khoan` (`MaTK`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `ck_xl_thoigian` CHECK (`DenNgay` IS NULL OR `DenNgay` >= `TuNgay`),
  CONSTRAINT `ck_xl_lydo` CHECK (`LyDoKetThuc` IS NULL OR CHAR_LENGTH(TRIM(`LyDoKetThuc`)) > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 20. PHAN_CONG_GIANG_DAY
CREATE TABLE `phan_cong_giang_day` (
  `MaPCGD` bigint unsigned NOT NULL AUTO_INCREMENT,
  `MaLMH` bigint unsigned NOT NULL,
  `MaGV` bigint unsigned NOT NULL,
  `TuNgay` date NOT NULL,
  `DenNgay` date DEFAULT NULL,
  `TrangThaiDuyet` varchar(20) NOT NULL DEFAULT 'CHO_DUYET',
  `MaTKDuyet` bigint unsigned DEFAULT NULL,
  `DuyetLuc` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`MaPCGD`),
  KEY `ix_PHAN_CONG_GIANG_DAY_MaLMH_TrangThaiDuyet_TuNgay_DenNgay` (`MaLMH`,`TrangThaiDuyet`,`TuNgay`,`DenNgay`),
  KEY `ix_PHAN_CONG_GIANG_DAY_MaGV_TuNgay` (`MaGV`,`TuNgay`),
  KEY `fk_PHAN_CONG_GIANG_DAY_MaTKDuyet` (`MaTKDuyet`),
  CONSTRAINT `fk_PHAN_CONG_GIANG_DAY_MaGV` FOREIGN KEY (`MaGV`) REFERENCES `giao_vien` (`MaGV`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_PHAN_CONG_GIANG_DAY_MaLMH` FOREIGN KEY (`MaLMH`) REFERENCES `lop_mon_hoc` (`MaLMH`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_PHAN_CONG_GIANG_DAY_MaTKDuyet` FOREIGN KEY (`MaTKDuyet`) REFERENCES `tai_khoan` (`MaTK`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `ck_MaPCGD_duyet` CHECK ((`TrangThaiDuyet` in ('CHO_DUYET','DA_DUYET','TU_CHOI'))),
  CONSTRAINT `ck_MaPCGD_nguoiduyet` CHECK ((((`TrangThaiDuyet` = 'CHO_DUYET') and (`MaTKDuyet` is null) and (`DuyetLuc` is null)) or ((`TrangThaiDuyet` in ('DA_DUYET','TU_CHOI')) and (`MaTKDuyet` is not null) and (`DuyetLuc` is not null)))),
  CONSTRAINT `ck_MaPCGD_thoigian` CHECK (((`DenNgay` is null) or (`DenNgay` > `TuNgay`)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 21. PHIEN_DIEM_DANH
CREATE TABLE `phien_diem_danh` (
  `MaPDD` bigint unsigned NOT NULL AUTO_INCREMENT,
  `MaLop` bigint unsigned NOT NULL,
  `MaHK` bigint unsigned NOT NULL,
  `MaLMH` bigint unsigned DEFAULT NULL,
  `Ngay` date NOT NULL,
  `PhamVi` varchar(10) NOT NULL,
  `MaKhung` varchar(30) NOT NULL,
  `TrangThai` varchar(20) NOT NULL DEFAULT 'DANG_NHAP',
  `MaTKTao` bigint unsigned NOT NULL,
  PRIMARY KEY (`MaPDD`),
  UNIQUE KEY `uk_pdd_lop_ngay_khung` (`MaLop`,`Ngay`,`PhamVi`,`MaKhung`),
  KEY `ix_PHIEN_DIEM_DANH_MaHK_Ngay` (`MaHK`,`Ngay`),
  KEY `fk_PHIEN_DIEM_DANH_MaLMH` (`MaLMH`),
  KEY `fk_PHIEN_DIEM_DANH_MaTKTao` (`MaTKTao`),
  CONSTRAINT `fk_PHIEN_DIEM_DANH_MaHK` FOREIGN KEY (`MaHK`) REFERENCES `hoc_ky` (`MaHK`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_PHIEN_DIEM_DANH_MaLMH` FOREIGN KEY (`MaLMH`) REFERENCES `lop_mon_hoc` (`MaLMH`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_PHIEN_DIEM_DANH_MaLop` FOREIGN KEY (`MaLop`) REFERENCES `lop_hoc` (`MaLop`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_PHIEN_DIEM_DANH_MaTKTao` FOREIGN KEY (`MaTKTao`) REFERENCES `tai_khoan` (`MaTK`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `ck_pdd_khung` CHECK ((((`PhamVi` = 'NGAY') and (`MaKhung` = 'CA_NGAY')) or ((`PhamVi` = 'BUOI') and (`MaKhung` in ('SANG','CHIEU','TOI'))) or ((`PhamVi` = 'TIET') and (char_length(trim(`MaKhung`)) > 0)))),
  CONSTRAINT `ck_pdd_mon` CHECK ((((`PhamVi` = 'TIET') and (`MaLMH` is not null)) or ((`PhamVi` in ('NGAY','BUOI')) and (`MaLMH` is null)))),
  CONSTRAINT `ck_pdd_phamvi` CHECK ((`PhamVi` in ('NGAY','BUOI','TIET'))),
  CONSTRAINT `ck_pdd_trangthai` CHECK ((`TrangThai` in ('DANG_NHAP','DA_CHOT')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 22. DIEM_DANH
CREATE TABLE `diem_danh` (
  `MaDD` bigint unsigned NOT NULL AUTO_INCREMENT,
  `MaPDD` bigint unsigned NOT NULL,
  `MaXL` bigint unsigned NOT NULL,
  `TrangThai` varchar(25) NOT NULL DEFAULT 'CHUA_GHI_NHAN',
  `SoPhutMuon` smallint unsigned NOT NULL DEFAULT '0',
  `GhiChu` varchar(500) DEFAULT NULL,
  `MaTKCapNhat` bigint unsigned NOT NULL,
  `CapNhatLuc` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`MaDD`),
  UNIQUE KEY `uk_dd_phien_xeplop` (`MaPDD`,`MaXL`),
  KEY `fk_DIEM_DANH_MaXL` (`MaXL`),
  KEY `fk_DIEM_DANH_MaTKCapNhat` (`MaTKCapNhat`),
  CONSTRAINT `fk_DIEM_DANH_MaPDD` FOREIGN KEY (`MaPDD`) REFERENCES `phien_diem_danh` (`MaPDD`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_DIEM_DANH_MaTKCapNhat` FOREIGN KEY (`MaTKCapNhat`) REFERENCES `tai_khoan` (`MaTK`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_DIEM_DANH_MaXL` FOREIGN KEY (`MaXL`) REFERENCES `xep_lop` (`MaXL`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `ck_dd_muon` CHECK (((`TrangThai` = 'CO_MAT') or (`SoPhutMuon` = 0))),
  CONSTRAINT `ck_dd_trangthai` CHECK ((`TrangThai` in ('CHUA_GHI_NHAN','CO_MAT','VANG_CO_PHEP','VANG_KHONG_PHEP')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 23. LAN_DANH_GIA
CREATE TABLE `lan_danh_gia` (
  `MaLDG` bigint unsigned NOT NULL AUTO_INCREMENT,
  `MaKQM` bigint unsigned NOT NULL,
  `MaPCGD` bigint unsigned NOT NULL,
  `LoaiDanhGia` varchar(20) NOT NULL,
  `SoThuTu` smallint unsigned NOT NULL,
  `NgayDanhGia` date NOT NULL,
  `DiemSo` decimal(3,1) DEFAULT NULL,
  `MucDanhGia` varchar(20) DEFAULT NULL,
  `NhanXet` text,
  `TrangThai` varchar(20) NOT NULL DEFAULT 'CHUA_DANH_GIA',
  `MaTKCapNhat` bigint unsigned NOT NULL,
  `CapNhatLuc` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`MaLDG`),
  UNIQUE KEY `uk_ldg_kqm_loai_stt` (`MaKQM`,`LoaiDanhGia`,`SoThuTu`),
  KEY `fk_LAN_DANH_GIA_MaPCGD` (`MaPCGD`),
  KEY `fk_LAN_DANH_GIA_MaTKCapNhat` (`MaTKCapNhat`),
  CONSTRAINT `fk_LAN_DANH_GIA_MaKQM` FOREIGN KEY (`MaKQM`) REFERENCES `ket_qua_mon` (`MaKQM`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_LAN_DANH_GIA_MaPCGD` FOREIGN KEY (`MaPCGD`) REFERENCES `phan_cong_giang_day` (`MaPCGD`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_LAN_DANH_GIA_MaTKCapNhat` FOREIGN KEY (`MaTKCapNhat`) REFERENCES `tai_khoan` (`MaTK`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `ck_ldg_diem` CHECK (((`DiemSo` is null) or (`DiemSo` between 0 and 10))),
  CONSTRAINT `ck_ldg_giatri` CHECK ((((`TrangThai` = 'CHUA_DANH_GIA') and (`DiemSo` is null) and (`MucDanhGia` is null)) or ((`TrangThai` = 'DA_DANH_GIA') and (((`DiemSo` is not null) and (`MucDanhGia` is null)) or ((`DiemSo` is null) and (`MucDanhGia` is not null)))))),
  CONSTRAINT `ck_ldg_loai` CHECK ((`LoaiDanhGia` in ('THUONG_XUYEN','GIUA_KY','CUOI_KY'))),
  CONSTRAINT `ck_ldg_muc` CHECK (((`MucDanhGia` is null) or (`MucDanhGia` in ('DAT','CHUA_DAT')))),
  CONSTRAINT `ck_ldg_stt` CHECK (((`SoThuTu` > 0) and ((`LoaiDanhGia` = 'THUONG_XUYEN') or (`SoThuTu` = 1)))),
  CONSTRAINT `ck_ldg_trangthai` CHECK ((`TrangThai` in ('CHUA_DANH_GIA','DA_DANH_GIA')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
