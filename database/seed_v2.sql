-- Optional demo for highschool_management_v2.sql. Run ONCE, stop on error.
-- Demo password: Test@12345 (BCrypt). Demo accounts only.
USE highschool_management_v2;
SET NAMES utf8mb4;
SET SESSION time_zone = '+07:00';
START TRANSACTION;
INSERT INTO khoi_lop (MaKhoi,SoKhoi) VALUES (1,10),(2,11),(3,12);
INSERT INTO vai_tro (MaVT,MaSoVaiTro,TenVaiTro) VALUES
(1,'ADMIN','Quản trị'),(2,'BGH','Ban giám hiệu'),(3,'GIAO_VU','Giáo vụ'),(4,'GIAO_VIEN','Giáo viên');
INSERT INTO nam_hoc (MaNH,MaSoNH,NgayBatDau,NgayKetThuc) VALUES
(1,'2026-2027','2026-09-01','2027-05-31'),(2,'2027-2028','2027-09-01','2028-05-31');
INSERT INTO hoc_ky (MaHK,MaNH,SoHocKy,NgayBatDau,NgayKetThuc,TrangThai) VALUES
(1,1,1,'2026-09-01','2027-01-10','DANG_DIEN_RA'),(2,1,2,'2027-01-11','2027-05-31','CHUA_BAT_DAU');
INSERT INTO giao_vien (MaGV,MaSoGV,HoTen,ChuyenMon) VALUES
(1,'GV001','Hoàng Minh Tuấn','Toán'),(2,'GV002','Phạm Quỳnh Nga','Ngữ văn');
INSERT INTO tai_khoan (MaTK,MaGV,TenDangNhap,MatKhauBam,HoTen) VALUES
(1,NULL,'demo_giaovu','$2a$10$QCgbrfozJ55A6KZNuDN0w.uFrOArTJgFuMyDsOSzAtfkz.tWqxO3C','Giáo vụ demo'),
(2,1,'demo_giaovien','$2a$10$QCgbrfozJ55A6KZNuDN0w.uFrOArTJgFuMyDsOSzAtfkz.tWqxO3C','Hoàng Minh Tuấn'),
(3,NULL,'demo_bgh','$2a$10$QCgbrfozJ55A6KZNuDN0w.uFrOArTJgFuMyDsOSzAtfkz.tWqxO3C','BGH demo');
INSERT INTO tai_khoan_vai_tro (MaTK,MaVT) VALUES (1,3),(2,4),(3,2);
INSERT INTO hoc_sinh (MaHS,MaSoHS,HoTen,NgaySinh,GioiTinh,NgayTiepNhan) VALUES
(1,'HS001','Nguyễn Văn An','2011-05-15','NAM','2026-08-20'),
(2,'HS002','Trần Thị Bình','2011-08-20','NU','2026-08-20'),
(3,'HS003','Lê Minh Châu','2011-03-12','NU','2026-08-21');
INSERT INTO nguoi_giam_ho (MaNGH,HoTen,SoDienThoai) VALUES
(1,'Nguyễn Văn Hùng','0987654321'),(2,'Trần Thị Mai','0987654322');
INSERT INTO hs_nguoi_giam_ho (MaHS,MaNGH,MoiQuanHe,LienHeChinh) VALUES (1,1,'CHA',1),(2,2,'ME',1);
INSERT INTO lop_hoc (MaLop,MaNH,MaKhoi,TenLop) VALUES
(1,1,1,'10A1'),(2,1,1,'10A2'),(3,1,2,'11A1'),(4,2,2,'11A1');
INSERT INTO mon_hoc (MaMH,MaSoMon,TenMon) VALUES (1,'TOAN','Toán'),(2,'VAN','Ngữ văn');
INSERT INTO lop_mon_hoc (MaLMH,MaLop,MaMH,MaHK,HinhThucDanhGia,SoTietNam) VALUES
(1,1,1,1,'DIEM_SO_KEM_NHAN_XET',105),(2,2,1,1,'DIEM_SO_KEM_NHAN_XET',105);
INSERT INTO phan_cong_chu_nhiem
(MaPCCN,MaLop,MaGV,TuNgay,TrangThaiDuyet,MaTKDuyet,DuyetLuc) VALUES
(1,1,1,'2026-09-01','DA_DUYET',3,'2026-08-25 08:00:00');
INSERT INTO phan_cong_giang_day
(MaPCGD,MaLMH,MaGV,TuNgay,TrangThaiDuyet,MaTKDuyet,DuyetLuc) VALUES
(1,1,1,'2026-09-01','DA_DUYET',3,'2026-08-25 08:00:00'),
(2,2,1,'2026-09-01','DA_DUYET',3,'2026-08-25 08:00:00');
-- An transfers on 15 October. Binh transfers in the future on 15 November.
-- Chau has no enrollment. An also has next-year placement despite old NULL end.
INSERT INTO xep_lop
(MaXL,MaHS,MaLop,TuNgay,DenNgay,LyDoKetThuc,MaTKNguoiThucHien,ThoiDiemThucHien) VALUES
(1,1,1,'2026-09-01','2026-10-14','Chuyển theo nguyện vọng',1,'2026-08-28 08:00:00'),
(2,1,2,'2026-10-15',NULL,NULL,1,'2026-10-10 08:00:00'),
(3,2,1,'2026-09-01','2026-11-14','Điều chỉnh lớp',1,'2026-08-28 08:00:00'),
(4,2,2,'2026-11-15',NULL,NULL,1,'2026-11-01 08:00:00'),
(5,1,4,'2027-09-01',NULL,NULL,1,'2027-08-25 08:00:00');
INSERT INTO nhat_ky_he_thong (MaTK,HanhDong,LoaiDoiTuong,MaDoiTuong,DuLieuTruoc,DuLieuSau,ThoiDiem)
VALUES (1,'CHUYEN_LOP','XEP_LOP','1',JSON_OBJECT('MaXL',1,'DenNgay',NULL),
JSON_OBJECT('MaXLNguon',1,'MaXLDich',2,'DenNgayNguon','2026-10-14','NgayChuyen','2026-10-15'),
'2026-10-10 08:00:00');
INSERT INTO ket_qua_mon (MaKQM,MaHS,MaMH,MaHK,HinhThucDanhGia)
VALUES (1,1,1,1,'DIEM_SO_KEM_NHAN_XET');
INSERT INTO lan_danh_gia
(MaKQM,MaPCGD,LoaiDanhGia,SoThuTu,NgayDanhGia,DiemSo,TrangThai,MaTKCapNhat)
VALUES (1,1,'THUONG_XUYEN',1,'2026-10-01',8.5,'DA_DANH_GIA',2);
INSERT INTO phien_diem_danh (MaPDD,MaLop,MaHK,Ngay,PhamVi,MaKhung,MaTKTao)
VALUES (1,1,1,'2026-10-01','NGAY','CA_NGAY',2);
INSERT INTO diem_danh (MaPDD,MaXL,TrangThai,MaTKCapNhat) VALUES (1,1,'CO_MAT',2),(1,3,'CO_MAT',2);
INSERT INTO tong_ket_hoc_ky (MaHS,MaHK) VALUES (1,1),(2,1);
INSERT INTO tong_ket_nam (MaHS,MaNH) VALUES (1,1),(2,1);
COMMIT;
