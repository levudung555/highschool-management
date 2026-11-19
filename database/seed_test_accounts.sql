-- Tai khoan mau cho moi truong local/test.
-- bgh_test    / Demo@123456 : Ban giam hieu
-- giaovu_test / Demo@123456 : Giao vu
-- Mat khau duoc bam bang BCrypt (cost 10).
-- Chay lai khong tao ban ghi trung; giu nguyen mat khau/trang thai tai khoan da co.

USE highschool_management_v2;
SET NAMES utf8mb4;

START TRANSACTION;

INSERT INTO vai_tro (MaSoVaiTro, TenVaiTro)
SELECT 'BGH', 'Ban giám hiệu'
WHERE NOT EXISTS (SELECT 1 FROM vai_tro WHERE MaSoVaiTro = 'BGH');

INSERT INTO vai_tro (MaSoVaiTro, TenVaiTro)
SELECT 'GIAO_VU', 'Giáo vụ'
WHERE NOT EXISTS (SELECT 1 FROM vai_tro WHERE MaSoVaiTro = 'GIAO_VU');

INSERT INTO tai_khoan (TenDangNhap, MatKhauBam, HoTen, TrangThai)
SELECT 'bgh_test',
       '$2a$10$HfEnt/rn/1XG5qQUDtTe1eYxnfg2d4pfKaYW91n5cpIxJmBDbJD32',
       'Ban giám hiệu test', 'HOAT_DONG'
WHERE NOT EXISTS (SELECT 1 FROM tai_khoan WHERE TenDangNhap = 'bgh_test');

INSERT INTO tai_khoan (TenDangNhap, MatKhauBam, HoTen, TrangThai)
SELECT 'giaovu_test',
       '$2a$10$HfEnt/rn/1XG5qQUDtTe1eYxnfg2d4pfKaYW91n5cpIxJmBDbJD32',
       'Giáo vụ test', 'HOAT_DONG'
WHERE NOT EXISTS (SELECT 1 FROM tai_khoan WHERE TenDangNhap = 'giaovu_test');

INSERT INTO tai_khoan_vai_tro (MaTK, MaVT)
SELECT t.MaTK, v.MaVT
FROM tai_khoan t
JOIN vai_tro v
  ON (t.TenDangNhap = 'bgh_test' AND v.MaSoVaiTro = 'BGH')
  OR (t.TenDangNhap = 'giaovu_test' AND v.MaSoVaiTro = 'GIAO_VU')
WHERE NOT EXISTS (
    SELECT 1 FROM tai_khoan_vai_tro tv
    WHERE tv.MaTK = t.MaTK AND tv.MaVT = v.MaVT
);

COMMIT;

-- Kiem tra tai khoan va quyen sau khi insert.
SELECT t.MaTK, t.TenDangNhap, t.HoTen, t.TrangThai, v.MaSoVaiTro
FROM tai_khoan t
JOIN tai_khoan_vai_tro tv ON tv.MaTK = t.MaTK
JOIN vai_tro v ON v.MaVT = tv.MaVT
WHERE t.TenDangNhap IN ('bgh_test', 'giaovu_test')
ORDER BY t.TenDangNhap, v.MaSoVaiTro;
