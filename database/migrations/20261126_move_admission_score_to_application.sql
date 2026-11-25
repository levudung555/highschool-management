-- Chay mot lan tren database cu truoc khi deploy code import moi.
-- Luu diem cu de khong mat du lieu, sau do tao ho so cho hoc sinh da co lop khoi 10
-- va lop da duoc gan to hop. Hoc sinh chua co du nam hoc/to hop duoc giu trong bang backup.
CREATE TABLE IF NOT EXISTS hoc_sinh_diem_dau_vao_legacy_backup (
  MaHS BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  MaSoHS VARCHAR(30) NOT NULL,
  DiemDauVao DECIMAL(4,2) NOT NULL,
  SaoLuuLuc DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT IGNORE INTO hoc_sinh_diem_dau_vao_legacy_backup (MaHS, MaSoHS, DiemDauVao)
SELECT MaHS, MaSoHS, DiemDauVao
FROM hoc_sinh;

INSERT IGNORE INTO ho_so_nhap_hoc (MaHS, MaNH, MaToHop, TongDiemDauVao)
SELECT x.MaHS, x.MaNH, x.MaToHop, x.DiemDauVao
FROM (
  SELECT
    hs.MaHS,
    hs.DiemDauVao,
    lop.MaNH,
    lop.MaToHop,
    ROW_NUMBER() OVER (PARTITION BY hs.MaHS ORDER BY xl.TuNgay, xl.MaXL) AS ThuTu
  FROM hoc_sinh hs
  JOIN xep_lop xl ON xl.MaHS = hs.MaHS
  JOIN lop_hoc lop ON lop.MaLop = xl.MaLop
  JOIN khoi_lop khoi ON khoi.MaKhoi = lop.MaKhoi AND khoi.SoKhoi = 10
  WHERE lop.MaToHop IS NOT NULL
) x
WHERE x.ThuTu = 1
  AND NOT EXISTS (
    SELECT 1
    FROM ho_so_nhap_hoc hsnh
    WHERE hsnh.MaHS = x.MaHS AND hsnh.MaNH = x.MaNH
  );

ALTER TABLE hoc_sinh DROP COLUMN DiemDauVao;
