-- Read-only examples. XEP_LOP intervals are inclusive, bounded by academic year.
USE highschool_management_v2;
SET @maNamHoc = 1;
SET @maLop = 1;
SET @maHocSinh = 1;
SET @ngayXem = DATE('2026-11-08');

-- 1. Never enrolled in the selected academic year, including future placements.
SELECT h.MaHS,h.MaSoHS,h.HoTen,h.NgayTiepNhan
FROM hoc_sinh h WHERE NOT EXISTS (
  SELECT 1 FROM xep_lop x JOIN lop_hoc l ON l.MaLop=x.MaLop
  WHERE x.MaHS=h.MaHS AND l.MaNH=@maNamHoc
) ORDER BY h.MaHS;

-- 2. Class roster at a date.
SELECT x.MaXL,h.MaHS,h.MaSoHS,h.HoTen,x.TuNgay,x.DenNgay
FROM xep_lop x JOIN hoc_sinh h ON h.MaHS=x.MaHS
JOIN lop_hoc l ON l.MaLop=x.MaLop JOIN nam_hoc n ON n.MaNH=l.MaNH
WHERE x.MaLop=@maLop AND @ngayXem BETWEEN n.NgayBatDau AND n.NgayKetThuc
AND x.TuNgay<=@ngayXem AND (x.DenNgay IS NULL OR x.DenNgay>=@ngayXem)
ORDER BY h.HoTen,h.MaHS;

-- 3. Class size with exactly the same date predicate.
SELECT COUNT(DISTINCT x.MaHS) AS SiSo
FROM xep_lop x JOIN lop_hoc l ON l.MaLop=x.MaLop JOIN nam_hoc n ON n.MaNH=l.MaNH
WHERE x.MaLop=@maLop AND @ngayXem BETWEEN n.NgayBatDau AND n.NgayKetThuc
AND x.TuNgay<=@ngayXem AND (x.DenNgay IS NULL OR x.DenNgay>=@ngayXem);

-- 4. Current class. Do not LIMIT 1 to hide overlapping data.
SELECT x.MaXL,l.MaLop,l.TenLop,n.MaSoNH,x.TuNgay,x.DenNgay
FROM xep_lop x JOIN lop_hoc l ON l.MaLop=x.MaLop JOIN nam_hoc n ON n.MaNH=l.MaNH
WHERE x.MaHS=@maHocSinh AND l.MaNH=@maNamHoc
AND @ngayXem BETWEEN n.NgayBatDau AND n.NgayKetThuc
AND x.TuNgay<=@ngayXem AND (x.DenNgay IS NULL OR x.DenNgay>=@ngayXem);

-- 5. History, with original creator and creation timestamp.
SELECT x.MaXL,l.TenLop,n.MaSoNH,x.TuNgay,x.DenNgay,x.LyDoKetThuc,
       x.MaTKNguoiThucHien,t.HoTen AS NguoiThucHien,x.ThoiDiemThucHien
FROM xep_lop x JOIN lop_hoc l ON l.MaLop=x.MaLop
JOIN nam_hoc n ON n.MaNH=l.MaNH JOIN tai_khoan t ON t.MaTK=x.MaTKNguoiThucHien
WHERE x.MaHS=@maHocSinh ORDER BY x.TuNgay DESC,x.MaXL DESC;

-- 6. Integrity audit: overlapping intervals within the same academic year.
SELECT a.MaHS,a.MaXL AS MaXL1,b.MaXL AS MaXL2,la.MaNH
FROM xep_lop a JOIN xep_lop b ON b.MaHS=a.MaHS AND b.MaXL>a.MaXL
JOIN lop_hoc la ON la.MaLop=a.MaLop JOIN lop_hoc lb ON lb.MaLop=b.MaLop AND lb.MaNH=la.MaNH
JOIN nam_hoc n ON n.MaNH=la.MaNH
WHERE a.TuNgay<=COALESCE(b.DenNgay,n.NgayKetThuc)
AND b.TuNgay<=COALESCE(a.DenNgay,n.NgayKetThuc);
