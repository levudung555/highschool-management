CREATE TABLE IF NOT EXISTS giao_vien (
  MaGV bigint NOT NULL AUTO_INCREMENT,
  MaSoGV varchar(30) NOT NULL,
  HoTen varchar(150) NOT NULL,
  NgaySinh date DEFAULT NULL,
  SoDienThoai varchar(20) DEFAULT NULL,
  Email varchar(254) DEFAULT NULL,
  ChuyenMon varchar(150) DEFAULT NULL,
  TrangThai varchar(25) NOT NULL DEFAULT 'DANG_CONG_TAC',
  PRIMARY KEY (MaGV),
  UNIQUE KEY uk_gv_maso (MaSoGV),
  CONSTRAINT ck_gv_hoten CHECK ((char_length(trim(HoTen)) > 0)),
  CONSTRAINT ck_gv_maso CHECK ((char_length(trim(MaSoGV)) > 0)),
  CONSTRAINT ck_gv_trangthai CHECK ((TrangThai in ('DANG_CONG_TAC','NGUNG_CONG_TAC')))
);

CREATE TABLE IF NOT EXISTS hoc_sinh (
  MaHS bigint NOT NULL AUTO_INCREMENT,
  MaSoHS varchar(30) NOT NULL,
  HoTen varchar(150) NOT NULL,
  NgaySinh date NOT NULL,
  GioiTinh varchar(10) NOT NULL,
  DiaChi varchar(500) DEFAULT NULL,
  SoDienThoai varchar(20) DEFAULT NULL,
  NgayTiepNhan DATETIME NOT NULL,
  CMMD VARCHAR(100) NOT NULL,
  NgayCapNhat DATETIME NOT NULL,
  PRIMARY KEY (MaHS),
  UNIQUE KEY uk_hs_maso (MaSoHS),
  CONSTRAINT ck_hs_gioitinh CHECK ((GioiTinh in ('NAM','NU','KHAC'))),
  CONSTRAINT ck_hs_hoten CHECK ((char_length(trim(HoTen)) > 0)),
  CONSTRAINT ck_hs_maso CHECK ((char_length(trim(MaSoHS)) > 0)),
  CONSTRAINT ck_hs_tiepnhan CHECK (((NgayTiepNhan is null) or (NgayTiepNhan >= NgaySinh)))
);

CREATE TABLE IF NOT EXISTS khoi_lop (
  MaKhoi bigint NOT NULL AUTO_INCREMENT,
  SoKhoi tinyint NOT NULL,
  PRIMARY KEY (MaKhoi),
  UNIQUE KEY uk_khoi_so (SoKhoi),
  CONSTRAINT ck_khoi_so CHECK ((SoKhoi in (10,11,12)))
);

CREATE TABLE IF NOT EXISTS nam_hoc (
  MaNH bigint NOT NULL AUTO_INCREMENT,
  MaSoNH varchar(20) NOT NULL,
  NgayBatDau date NOT NULL,
  NgayKetThuc date NOT NULL,
  PRIMARY KEY (MaNH),
  UNIQUE KEY uk_nh_maso (MaSoNH),
  CONSTRAINT ck_nh_ngay CHECK ((NgayKetThuc > NgayBatDau))
);

CREATE TABLE IF NOT EXISTS vai_tro (
  MaVT bigint NOT NULL AUTO_INCREMENT,
  MaSoVaiTro varchar(40) NOT NULL,
  TenVaiTro varchar(100) NOT NULL,
  PRIMARY KEY (MaVT),
  UNIQUE KEY uk_vt_maso (MaSoVaiTro)
);

CREATE TABLE IF NOT EXISTS tai_khoan (
  MaTK bigint NOT NULL AUTO_INCREMENT,
  MaGV bigint DEFAULT NULL,
  TenDangNhap varchar(100) NOT NULL,
  MatKhauBam varchar(255) NOT NULL,
  HoTen varchar(150) NOT NULL,
  Email varchar(254) DEFAULT NULL,
  TrangThai varchar(20) NOT NULL DEFAULT 'HOAT_DONG',
  PRIMARY KEY (MaTK),
  UNIQUE KEY uk_tk_tendangnhap (TenDangNhap),
  UNIQUE KEY uk_tk_gv (MaGV),
  CONSTRAINT fk_TAI_KHOAN_MaGV FOREIGN KEY (MaGV) REFERENCES giao_vien (MaGV) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT ck_tk_matkhau CHECK ((char_length(trim(MatKhauBam)) > 0)),
  CONSTRAINT ck_tk_tendangnhap CHECK ((char_length(trim(TenDangNhap)) > 0)),
  CONSTRAINT ck_tk_trangthai CHECK ((TrangThai in ('HOAT_DONG','BI_KHOA')))
);

CREATE TABLE IF NOT EXISTS tai_khoan_vai_tro (
  MaTK bigint NOT NULL,
  MaVT bigint NOT NULL,
  PRIMARY KEY (MaTK,MaVT),
  CONSTRAINT fk_TAI_KHOAN_VAI_TRO_MaTK FOREIGN KEY (MaTK) REFERENCES tai_khoan (MaTK) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT fk_TAI_KHOAN_VAI_TRO_MaVT FOREIGN KEY (MaVT) REFERENCES vai_tro (MaVT) ON DELETE RESTRICT ON UPDATE RESTRICT
);

CREATE TABLE IF NOT EXISTS lop_hoc (
  MaToHop BIGINT,
  SiSoToiDa INTEGER,
  ThuTuXep INTEGER,
  MaLop bigint NOT NULL AUTO_INCREMENT,
  MaNH bigint NOT NULL,
  MaKhoi bigint NOT NULL,
  TenLop varchar(30) NOT NULL,
  TrangThai varchar(20) NOT NULL DEFAULT 'HOAT_DONG',
  PRIMARY KEY (MaLop),
  UNIQUE KEY uk_lop_nam_ten (MaNH,TenLop),
  CONSTRAINT fk_LOP_HOC_MaKhoi FOREIGN KEY (MaKhoi) REFERENCES khoi_lop (MaKhoi) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT fk_LOP_HOC_MaNH FOREIGN KEY (MaNH) REFERENCES nam_hoc (MaNH) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT ck_lop_ten CHECK ((char_length(trim(TenLop)) > 0)),
  CONSTRAINT ck_lop_trangthai CHECK ((TrangThai in ('HOAT_DONG','NGUNG_HOAT_DONG')))
);

CREATE TABLE IF NOT EXISTS xep_lop (
  MaHoSoNhapHoc BIGINT UNIQUE,
  MaXL bigint NOT NULL AUTO_INCREMENT,
  MaHS bigint NOT NULL,
  MaLop bigint NOT NULL,
  TuNgay date NOT NULL,
  DenNgay date DEFAULT NULL,
  LyDoKetThuc varchar(255) DEFAULT NULL,
  MaTKNguoiThucHien bigint NOT NULL,
  ThoiDiemThucHien datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (MaXL),
  CONSTRAINT fk_XEP_LOP_MaHS FOREIGN KEY (MaHS) REFERENCES hoc_sinh (MaHS) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT fk_XEP_LOP_MaLop FOREIGN KEY (MaLop) REFERENCES lop_hoc (MaLop) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT fk_XEP_LOP_MaTKNguoiThucHien FOREIGN KEY (MaTKNguoiThucHien) REFERENCES tai_khoan (MaTK) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT ck_xl_thoigian CHECK (DenNgay IS NULL OR DenNgay >= TuNgay),
  CONSTRAINT ck_xl_lydo CHECK (LyDoKetThuc IS NULL OR CHAR_LENGTH(TRIM(LyDoKetThuc)) > 0)
);

CREATE TABLE IF NOT EXISTS invalidated_tokens (
    id VARCHAR(64) PRIMARY KEY,
    expire_time TIMESTAMP NOT NULL
);

DELETE FROM tai_khoan_vai_tro;
DELETE FROM xep_lop;
DELETE FROM tai_khoan;
DELETE FROM vai_tro;
DELETE FROM invalidated_tokens;
DELETE FROM lop_hoc;
DELETE FROM khoi_lop;
DELETE FROM nam_hoc;
DELETE FROM hoc_sinh;

INSERT INTO vai_tro (MaVT, MaSoVaiTro, TenVaiTro) VALUES 
(1, 'BGH', 'Ban Giám Hiệu'),
(2, 'GIAO_VU', 'Giáo Vụ'),
(3, 'ADMIN', 'Quản Trị Viên'),
(4, 'GIAO_VIEN', 'Giáo Viên');

INSERT INTO tai_khoan (MaTK, TenDangNhap, MatKhauBam, HoTen, TrangThai) VALUES
(1, 'bgh', '$2a$10$lpqJd.gZUdWI0DibQcsMCONg3kJTTrvhEzrmhG/kBS/G2uJJyFMFe', 'Ban Giám Hiệu', 'HOAT_DONG'),
(2, 'giaovu', '$2a$10$lpqJd.gZUdWI0DibQcsMCONg3kJTTrvhEzrmhG/kBS/G2uJJyFMFe', 'Giáo Vụ', 'HOAT_DONG'),
(3, 'locked', '$2a$10$lpqJd.gZUdWI0DibQcsMCONg3kJTTrvhEzrmhG/kBS/G2uJJyFMFe', 'Tài khoản khóa', 'BI_KHOA');

INSERT INTO tai_khoan_vai_tro (MaTK, MaVT) VALUES
(1, 1),
(2, 2),
(3, 2);

INSERT INTO nam_hoc (MaNH, MaSoNH, NgayBatDau, NgayKetThuc) VALUES
(1, '2023-2024', '2023-09-05', '2024-05-31');

INSERT INTO khoi_lop (MaKhoi, SoKhoi) VALUES
(1, 10);

INSERT INTO lop_hoc (MaLop, MaNH, MaKhoi, TenLop, TrangThai) VALUES
(1, 1, 1, '10A1', 'HOAT_DONG');

INSERT INTO hoc_sinh (MaHS, MaSoHS, HoTen, NgaySinh, GioiTinh, NgayTiepNhan, CMMD, NgayCapNhat) VALUES
(1, 'HS0001', 'Nguyen Van A', '2008-01-01', 'NAM', '2023-09-05', '000000000001', CURRENT_TIMESTAMP, 8.50);

INSERT INTO xep_lop (MaXL, MaHS, MaLop, TuNgay, DenNgay, LyDoKetThuc, MaTKNguoiThucHien, ThoiDiemThucHien) VALUES
(1, 1, 1, '2023-09-05', NULL, NULL, 1, CURRENT_TIMESTAMP);
