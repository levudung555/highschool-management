-- Tài khoản demo local có mật khẩu Test@12345, lưu BCrypt.
INSERT INTO vai_tro (MaSoVaiTro, TenVaiTro)
SELECT 'BGH', 'Ban Giám hiệu' WHERE NOT EXISTS (
    SELECT 1 FROM vai_tro WHERE MaSoVaiTro = 'BGH'
);
INSERT INTO tai_khoan (TenDangNhap, MatKhauBam, HoTen, TrangThai)
SELECT 'uc09_local_bgh', '$2a$10$QCgbrfozJ55A6KZNuDN0w.uFrOArTJgFuMyDsOSzAtfkz.tWqxO3C', 'BGH demo local', 'HOAT_DONG'
WHERE NOT EXISTS (SELECT 1 FROM tai_khoan WHERE TenDangNhap = 'uc09_local_bgh');
INSERT INTO tai_khoan_vai_tro (MaTK, MaVT)
SELECT t.MaTK, v.MaVT FROM tai_khoan t CROSS JOIN vai_tro v
WHERE t.TenDangNhap = 'uc09_local_bgh' AND v.MaSoVaiTro = 'BGH'
AND NOT EXISTS (SELECT 1 FROM tai_khoan_vai_tro tv WHERE tv.MaTK = t.MaTK AND tv.MaVT = v.MaVT);

-- Nâng cấp duy nhất tài khoản demo cũ chưa có mật khẩu; không đặt lại mật khẩu đã thay đổi.
UPDATE tai_khoan SET MatKhauBam = '$2a$10$QCgbrfozJ55A6KZNuDN0w.uFrOArTJgFuMyDsOSzAtfkz.tWqxO3C'
WHERE TenDangNhap = 'uc09_local_bgh' AND MatKhauBam = '!LOCAL_DEMO_NO_PASSWORD!';
INSERT INTO vai_tro (MaSoVaiTro, TenVaiTro)
SELECT 'GIAO_VU', 'Nhân viên giáo vụ' WHERE NOT EXISTS (SELECT 1 FROM vai_tro WHERE MaSoVaiTro='GIAO_VU');
INSERT INTO tai_khoan (TenDangNhap, MatKhauBam, HoTen, TrangThai)
SELECT 'uc09_local_giaovu','$2a$10$QCgbrfozJ55A6KZNuDN0w.uFrOArTJgFuMyDsOSzAtfkz.tWqxO3C','Giáo vụ demo local','HOAT_DONG'
WHERE NOT EXISTS (SELECT 1 FROM tai_khoan WHERE TenDangNhap='uc09_local_giaovu');
INSERT INTO tai_khoan_vai_tro (MaTK,MaVT)
SELECT t.MaTK,v.MaVT FROM tai_khoan t CROSS JOIN vai_tro v
WHERE t.TenDangNhap='uc09_local_giaovu' AND v.MaSoVaiTro='GIAO_VU'
AND NOT EXISTS (SELECT 1 FROM tai_khoan_vai_tro tv WHERE tv.MaTK=t.MaTK AND tv.MaVT=v.MaVT);
