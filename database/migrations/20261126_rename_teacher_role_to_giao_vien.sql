-- Chạy một lần để chuyển tên role TEACHER cũ sang GIAO_VIEN.
START TRANSACTION;

INSERT IGNORE INTO vai_tro (MaSoVaiTro, TenVaiTro)
VALUES ('GIAO_VIEN', 'Giáo viên');

INSERT IGNORE INTO tai_khoan_vai_tro (MaTK, MaVT)
SELECT tv.MaTK, chuan.MaVT
FROM tai_khoan_vai_tro tv
JOIN vai_tro cu ON cu.MaVT = tv.MaVT AND cu.MaSoVaiTro = 'TEACHER'
JOIN vai_tro chuan ON chuan.MaSoVaiTro = 'GIAO_VIEN';

DELETE tv
FROM tai_khoan_vai_tro tv
JOIN vai_tro cu ON cu.MaVT = tv.MaVT
WHERE cu.MaSoVaiTro = 'TEACHER';

DELETE FROM vai_tro WHERE MaSoVaiTro = 'TEACHER';
UPDATE vai_tro SET TenVaiTro = 'Giáo viên' WHERE MaSoVaiTro = 'GIAO_VIEN';

COMMIT;
