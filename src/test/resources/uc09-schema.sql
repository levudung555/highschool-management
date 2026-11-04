CREATE TABLE IF NOT EXISTS tai_khoan (
    MaTK BIGINT PRIMARY KEY,
    TenDangNhap VARCHAR(100) NOT NULL UNIQUE,
    MatKhauBam VARCHAR(255) NOT NULL,
    HoTen VARCHAR(150) NOT NULL,
    TrangThai VARCHAR(25) DEFAULT 'HOAT_DONG',
    TokenVersion BIGINT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS vai_tro (
    MaVT BIGINT PRIMARY KEY,
    MaSoVaiTro VARCHAR(50) NOT NULL UNIQUE,
    TenVaiTro VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS tai_khoan_vai_tro (
    MaTK BIGINT NOT NULL,
    MaVT BIGINT NOT NULL,
    PRIMARY KEY (MaTK, MaVT)
);

CREATE TABLE IF NOT EXISTS invalidated_token (
    id VARCHAR(255) PRIMARY KEY,
    expire_time TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS nam_hoc (
    MaNH BIGINT PRIMARY KEY,
    TenNH VARCHAR(100),
    NgayBatDau DATE,
    NgayKetThuc DATE,
    TrangThai VARCHAR(25)
);

CREATE TABLE IF NOT EXISTS khoi_lop (
    MaKhoi BIGINT PRIMARY KEY,
    TenKhoi VARCHAR(50),
    MaCapHoc BIGINT
);

CREATE TABLE IF NOT EXISTS lop_hoc (
    MaLop BIGINT PRIMARY KEY,
    MaNH BIGINT,
    MaKhoi BIGINT,
    TenLop VARCHAR(50),
    TrangThai VARCHAR(25)
);

CREATE TABLE IF NOT EXISTS hoc_sinh (
    MaHS BIGINT PRIMARY KEY,
    MaSoHS VARCHAR(20),
    HoTen VARCHAR(100),
    NgaySinh DATE,
    GioiTinh VARCHAR(10),
    NgayTiepNhan DATE
);

CREATE TABLE IF NOT EXISTS xep_lop (
    MaXL BIGINT PRIMARY KEY,
    MaHS BIGINT,
    MaLop BIGINT,
    TuNgay DATE,
    DenNgay DATE,
    LyDoKetThuc VARCHAR(255),
    TrangThaiDuyet VARCHAR(25),
    MaTKDuyet BIGINT,
    DuyetLuc TIMESTAMP
);

CREATE TABLE IF NOT EXISTS yeu_cau_chuyen_lop (
    MaYCC BIGINT PRIMARY KEY,
    MaXLNguon BIGINT,
    MaXLDich BIGINT,
    LyDo VARCHAR(255)
);

DELETE FROM tai_khoan_vai_tro;
DELETE FROM tai_khoan;
DELETE FROM vai_tro;
DELETE FROM invalidated_token;
DELETE FROM yeu_cau_chuyen_lop;
DELETE FROM xep_lop;
DELETE FROM lop_hoc;
DELETE FROM khoi_lop;
DELETE FROM nam_hoc;
DELETE FROM hoc_sinh;

INSERT INTO vai_tro (MaVT, MaSoVaiTro, TenVaiTro) VALUES 
(1, 'BGH', 'Ban Giám Hiệu'),
(2, 'GIAO_VU', 'Giáo Vụ'),
(3, 'ADMIN', 'Quản Trị Viên'),
(4, 'GIAO_VIEN', 'Giáo Viên');

INSERT INTO tai_khoan (MaTK, TenDangNhap, MatKhauBam, HoTen, TrangThai, TokenVersion) VALUES
(1, 'bgh', '$2a$10$0ns4v7JclyWzwijc48x/yuzjGvfgS1x6xapRWO6vSyqkBGNdKUJiS', 'Ban Giám Hiệu', 'HOAT_DONG', 0),
(2, 'giaovu', '$2a$10$0ns4v7JclyWzwijc48x/yuzjGvfgS1x6xapRWO6vSyqkBGNdKUJiS', 'Giáo Vụ', 'HOAT_DONG', 0),
(3, 'locked', '$2a$10$0ns4v7JclyWzwijc48x/yuzjGvfgS1x6xapRWO6vSyqkBGNdKUJiS', 'Tài khoản khóa', 'BI_KHOA', 0);

INSERT INTO tai_khoan_vai_tro (MaTK, MaVT) VALUES
(1, 1),
(2, 2),
(3, 2);

INSERT INTO nam_hoc (MaNH, TenNH, NgayBatDau, NgayKetThuc, TrangThai) VALUES
(1, '2023-2024', '2023-09-05', '2024-05-31', 'HOAT_DONG');

INSERT INTO khoi_lop (MaKhoi, TenKhoi, MaCapHoc) VALUES
(1, 'Khối 10', 1);

INSERT INTO lop_hoc (MaLop, MaNH, MaKhoi, TenLop, TrangThai) VALUES
(1, 1, 1, '10A1', 'HOAT_DONG');

INSERT INTO hoc_sinh (MaHS, MaSoHS, HoTen, NgaySinh, GioiTinh, NgayTiepNhan) VALUES
(1, 'HS0001', 'Nguyen Van A', '2008-01-01', 'NAM', '2023-09-05');

INSERT INTO xep_lop (MaXL, MaHS, MaLop, TuNgay, DenNgay, LyDoKetThuc, TrangThaiDuyet, MaTKDuyet, DuyetLuc) VALUES
(1, 1, 1, '2023-09-05', NULL, NULL, 'CHO_DUYET', NULL, NULL);
