-- Du lieu mau UC10. Chi them ban ghi con thieu, khong sua/xoa du lieu cu.
-- Can nam hoc 2026-2027, khoi 10 va tai khoan uc09_local_giaovu da ton tai.
-- Chay dung file, dung khi gap loi; khong dung mysql --force.
SET NAMES utf8mb4;
SET SESSION time_zone = '+07:00';
USE highschool_management_v2;
START TRANSACTION;
SET @uc10_nam = (SELECT MaNH FROM nam_hoc WHERE MaSoNH = '2026-2027');
SET @uc10_khoi = (SELECT MaKhoi FROM khoi_lop WHERE SoKhoi = 10);
SET @uc10_actor = (SELECT MaTK FROM tai_khoan WHERE TenDangNhap = 'uc09_local_giaovu' AND TrangThai = 'HOAT_DONG');

INSERT INTO hoc_ky (MaNH, SoHocKy, NgayBatDau, NgayKetThuc, TrangThai)
SELECT @uc10_nam, 1, '2026-09-01', '2027-01-15', 'DANG_DIEN_RA'
WHERE NOT EXISTS (SELECT 1 FROM hoc_ky WHERE MaNH = @uc10_nam AND SoHocKy = 1);
INSERT INTO hoc_ky (MaNH, SoHocKy, NgayBatDau, NgayKetThuc, TrangThai)
SELECT @uc10_nam, 2, '2027-01-18', '2027-05-31', 'CHUA_BAT_DAU'
WHERE NOT EXISTS (SELECT 1 FROM hoc_ky WHERE MaNH = @uc10_nam AND SoHocKy = 2);

INSERT INTO mon_hoc (MaSoMon, TenMon)
SELECT 'UC10-TOAN', 'Toán (UC10)' WHERE NOT EXISTS (SELECT 1 FROM mon_hoc WHERE MaSoMon = 'UC10-TOAN');
INSERT INTO mon_hoc (MaSoMon, TenMon)
SELECT 'UC10-VAN', 'Ngữ văn (UC10)' WHERE NOT EXISTS (SELECT 1 FROM mon_hoc WHERE MaSoMon = 'UC10-VAN');
INSERT INTO mon_hoc (MaSoMon, TenMon)
SELECT 'UC10-ANH', 'Tiếng Anh (UC10)' WHERE NOT EXISTS (SELECT 1 FROM mon_hoc WHERE MaSoMon = 'UC10-ANH');

INSERT INTO giao_vien (MaSoGV, HoTen, ChuyenMon, TrangThai)
SELECT 'UC10-GV01', 'Nguyễn Minh An (UC10)', 'Toán', 'DANG_CONG_TAC'
WHERE NOT EXISTS (SELECT 1 FROM giao_vien WHERE MaSoGV = 'UC10-GV01');
INSERT INTO giao_vien (MaSoGV, HoTen, ChuyenMon, TrangThai)
SELECT 'UC10-GV02', 'Trần Thu Hà (UC10)', 'Toán, Ngữ văn', 'DANG_CONG_TAC'
WHERE NOT EXISTS (SELECT 1 FROM giao_vien WHERE MaSoGV = 'UC10-GV02');
INSERT INTO giao_vien (MaSoGV, HoTen, ChuyenMon, TrangThai)
SELECT 'UC10-GV03', 'Lê Hoàng Nam (UC10)', 'Tiếng Anh', 'DANG_CONG_TAC'
WHERE NOT EXISTS (SELECT 1 FROM giao_vien WHERE MaSoGV = 'UC10-GV03');
INSERT INTO giao_vien (MaSoGV, HoTen, ChuyenMon, TrangThai)
SELECT 'UC10-GVNGHI', 'Giáo viên đã nghỉ (UC10)', 'Toán', 'NGUNG_CONG_TAC'
WHERE NOT EXISTS (SELECT 1 FROM giao_vien WHERE MaSoGV = 'UC10-GVNGHI');

INSERT INTO lop_hoc (MaNH, MaKhoi, TenLop, TrangThai)
SELECT @uc10_nam, @uc10_khoi, 'UC10-10A1', 'HOAT_DONG'
WHERE NOT EXISTS (SELECT 1 FROM lop_hoc WHERE MaNH = @uc10_nam AND TenLop = 'UC10-10A1');
INSERT INTO lop_hoc (MaNH, MaKhoi, TenLop, TrangThai)
SELECT @uc10_nam, @uc10_khoi, 'UC10-10A2', 'HOAT_DONG'
WHERE NOT EXISTS (SELECT 1 FROM lop_hoc WHERE MaNH = @uc10_nam AND TenLop = 'UC10-10A2');
INSERT INTO lop_hoc (MaNH, MaKhoi, TenLop, TrangThai)
SELECT @uc10_nam, @uc10_khoi, 'UC10-10DONG', 'NGUNG_HOAT_DONG'
WHERE NOT EXISTS (SELECT 1 FROM lop_hoc WHERE MaNH = @uc10_nam AND TenLop = 'UC10-10DONG');

INSERT INTO lop_mon_hoc (MaLop, MaMH, MaHK, HinhThucDanhGia, SoTietNam)
SELECT l.MaLop, m.MaMH, h.MaHK, 'DIEM_SO_KEM_NHAN_XET', 105
FROM lop_hoc l CROSS JOIN mon_hoc m JOIN hoc_ky h ON h.MaNH = l.MaNH
WHERE l.MaNH = @uc10_nam AND l.TenLop IN ('UC10-10A1', 'UC10-10A2', 'UC10-10DONG')
  AND m.MaSoMon IN ('UC10-TOAN', 'UC10-VAN', 'UC10-ANH')
  AND NOT EXISTS (SELECT 1 FROM lop_mon_hoc x WHERE x.MaLop = l.MaLop AND x.MaMH = m.MaMH AND x.MaHK = h.MaHK);

-- Lich su Toan A1: thay An day thang 9-10, co Ha tiep nhan tu 01/11.
-- Van A1: phan cong sap bat dau tu 01/12. A2 de trong cho nguoi dung tu thu.
INSERT INTO phan_cong_giang_day (MaLMH, MaGV, TuNgay, DenNgay, TrangThaiDuyet, MaTKDuyet, DuyetLuc)
SELECT x.MaLMH, g.MaGV, s.TuNgay, s.DenNgay, 'DA_DUYET', @uc10_actor, '2026-11-16 09:00:00'
FROM (
    SELECT 'UC10-TOAN' AS MaSoMon, 'UC10-GV01' AS MaSoGV, CAST('2026-09-01' AS DATE) AS TuNgay, CAST('2026-10-31' AS DATE) AS DenNgay
    UNION ALL SELECT 'UC10-TOAN', 'UC10-GV02', '2026-11-01', NULL
    UNION ALL SELECT 'UC10-VAN', 'UC10-GV02', '2026-12-01', NULL
) s
JOIN mon_hoc m ON m.MaSoMon = s.MaSoMon
JOIN giao_vien g ON g.MaSoGV = s.MaSoGV
JOIN lop_hoc l ON l.MaNH = @uc10_nam AND l.TenLop = 'UC10-10A1'
JOIN hoc_ky h ON h.MaNH = l.MaNH AND h.SoHocKy = 1
JOIN lop_mon_hoc x ON x.MaLop = l.MaLop AND x.MaMH = m.MaMH AND x.MaHK = h.MaHK
WHERE @uc10_actor IS NOT NULL
  AND s.TuNgay >= h.NgayBatDau AND COALESCE(s.DenNgay, h.NgayKetThuc) <= h.NgayKetThuc
  AND NOT EXISTS (
    SELECT 1 FROM phan_cong_giang_day p WHERE p.MaLMH = x.MaLMH AND p.TrangThaiDuyet = 'DA_DUYET'
      AND p.TuNgay <= COALESCE(s.DenNgay, h.NgayKetThuc)
      AND COALESCE(p.DenNgay, h.NgayKetThuc) >= s.TuNgay
  );

COMMIT;

SELECT x.MaLMH, l.TenLop, m.TenMon, h.SoHocKy, h.NgayBatDau, h.NgayKetThuc,
       (SELECT COUNT(*) FROM phan_cong_giang_day p WHERE p.MaLMH = x.MaLMH) AS SoPhanCong
FROM lop_mon_hoc x JOIN lop_hoc l ON l.MaLop = x.MaLop
JOIN mon_hoc m ON m.MaMH = x.MaMH JOIN hoc_ky h ON h.MaHK = x.MaHK
WHERE l.MaNH = @uc10_nam AND l.TenLop IN ('UC10-10A1', 'UC10-10A2', 'UC10-10DONG')
ORDER BY l.TenLop, h.SoHocKy, m.TenMon;
