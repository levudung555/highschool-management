-- Sample data for hoc_sinh
INSERT INTO hoc_sinh (MaSoHS, HoTen, NgaySinh, GioiTinh, DiaChi, SoDienThoai, Email, NgayTiepNhan)
VALUES 
('HS0001', 'Nguyễn Văn An', '2008-05-15', 'NAM', '123 Nguyễn Trãi, Hà Nội', '0912345671', 'an.nv@school.edu.vn', '2023-09-05'),
('HS0002', 'Trần Thị Bình', '2008-08-20', 'NU', '456 Lê Lợi, Hà Nội', '0912345672', 'binh.tt@school.edu.vn', '2023-09-05');

-- Sample data for giao_vien
INSERT INTO giao_vien (MaSoGV, HoTen, NgaySinh, SoDienThoai, Email, ChuyenMon, TrangThai)
VALUES 
('GV0001', 'Hoàng Minh Tuấn', '1985-09-12', '0901234567', 'tuan.hm@school.edu.vn', 'Toán học', 'DANG_CONG_TAC'),
('GV0002', 'Phạm Quỳnh Nga', '1990-04-18', '0907654321', 'nga.pq@school.edu.vn', 'Ngữ văn', 'DANG_CONG_TAC');

-- Sample data for nguoi_giam_ho
INSERT INTO nguoi_giam_ho (HoTen, SoDienThoai, Email, DiaChi)
VALUES 
('Nguyễn Văn Hùng', '0987654321', 'hung.nv@parent.com', '123 Nguyễn Trãi, Thanh Xuân, Hà Nội'),
('Lê Thị Mai', '0987654322', 'mai.lt@parent.com', '123 Nguyễn Trãi, Thanh Xuân, Hà Nội');

-- Sample data for hs_nguoi_giam_ho
INSERT INTO hs_nguoi_giam_ho (MaHS, MaNGH, MoiQuanHe, LienHeChinh)
VALUES 
(1, 1, 'Cha', 1),
(1, 2, 'Mẹ', 0);
