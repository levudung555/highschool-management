-- Sample data for HOC_SINH
INSERT INTO HOC_SINH (MaSoHS, HoTen, NgaySinh, GioiTinh, DiaChi, SoDienThoai, Email, NgayTiepNhan)
VALUES 
('HS0001', 'Nguyễn Văn An', '2008-05-15', 'NAM', '123 Nguyễn Trãi, Hà Nội', '0912345671', 'an.nv@school.edu.vn', '2023-09-05'),
('HS0002', 'Trần Thị Bình', '2008-08-20', 'NU', '456 Lê Lợi, Hà Nội', '0912345672', 'binh.tt@school.edu.vn', '2023-09-05');

-- Sample data for GIAO_VIEN
INSERT INTO GIAO_VIEN (MaSoGV, HoTen, NgaySinh, SoDienThoai, Email, ChuyenMon, TrangThai)
VALUES 
('GV0001', 'Hoàng Minh Tuấn', '1985-09-12', '0901234567', 'tuan.hm@school.edu.vn', 'Toán học', 'DANG_CONG_TAC'),
('GV0002', 'Phạm Quỳnh Nga', '1990-04-18', '0907654321', 'nga.pq@school.edu.vn', 'Ngữ văn', 'DANG_CONG_TAC');

-- Sample data for NGUOI_GIAM_HO
INSERT INTO NGUOI_GIAM_HO (HoTen, SoDienThoai, Email, DiaChi)
VALUES 
('Nguyễn Văn Hùng', '0987654321', 'hung.nv@parent.com', '123 Nguyễn Trãi, Thanh Xuân, Hà Nội'),
('Lê Thị Mai', '0987654322', 'mai.lt@parent.com', '123 Nguyễn Trãi, Thanh Xuân, Hà Nội');

-- Sample data for HS_NGUOI_GIAM_HO
INSERT INTO HS_NGUOI_GIAM_HO (MaHS, MaNGH, MoiQuanHe, LienHeChinh)
VALUES 
(1, 1, 'Cha', 1),
(1, 2, 'Mẹ', 0);
