# Bộ dữ liệu DEMOFULL26

Đã nạp vào MySQL local `highschool_management_v2` ngày 19/11/2026.
Dữ liệu giả lập, nhận diện bằng mã/tên `DEMOFULL26-`; không cập nhật hoặc xóa bản ghi cũ.

## Số lượng thêm

| Nội dung | Số lượng |
|---|---:|
| Học sinh | 330 |
| Giáo viên | 30 (28 đang công tác, 2 ngừng công tác) |
| Người giám hộ | 396 |
| Lớp | 21, thuộc 3 năm học |
| Môn học | 8 |
| Lớp–môn–học kỳ | 320 |
| Xếp lớp và lịch sử | 512 |
| Phân công chủ nhiệm | 19 |
| Phân công giảng dạy | 281 |
| Phiên điểm danh | 348 |
| Chi tiết điểm danh | 8.700 |
| Kết quả môn | 2.100 |
| Lần đánh giá | 7.500 |
| Tổng kết học kỳ / năm | 400 / 200 |
| Tài khoản | 15 |

Tổng cộng 21.597 bản ghi mới, gồm cả liên kết, danh mục và dấu ghi nhận nạp dữ liệu.

## Đăng nhập

Mật khẩu các tài khoản demo: `Test@12345`.

- `demofull26_admin`: ADMIN.
- `demofull26_giaovu`: GIAO_VU.
- `demofull26_bgh`: BGH.
- `demofull26_gv01` đến `demofull26_gv12`: GIAO_VIEN, liên kết giáo viên tương ứng.

Mật khẩu đã được đối chiếu BCrypt. Lúc kiểm tra, ứng dụng tại localhost:8080 chưa chạy nên chưa xác minh đăng nhập/API qua HTTP.

## Các tình huống thử

1. **Danh sách lớp:** chọn năm 2026–2027, `DEMOFULL26-10A1` đến `DEMOFULL26-12A4`. Mỗi lớp có 25 học sinh hiện tại, thử tìm kiếm và phân trang.
2. **Xếp lớp mới:** 30 học sinh có mã `DEMOFULL26-HS0301` đến `DEMOFULL26-HS0330` chưa xếp lớp. Lớp `DEMOFULL26-10TRONG` đang hoạt động và chưa có học sinh/phân công.
3. **Chuyển lớp:** 12 học sinh có lịch sử chuyển ngày 01/11/2026. Các học sinh khối 11–12 còn có lịch sử năm học 2025–2026.
4. **Chủ nhiệm:** các lớp thông thường có chủ nhiệm, dùng để thử chặn trùng. Lớp `DEMOFULL26-10TRONG` để thử tạo mới.
5. **Giảng dạy:** `DEMOFULL26-10A1`, Toán (DEMOFULL26), kỳ 1 năm 2026–2027 có hai giai đoạn giáo viên: đến 31/10 và từ 01/11. Tạo mới từ 19/11 trên cùng lớp–môn sẽ trùng.
6. **Giảng dạy mới:** chọn lớp `DEMOFULL26-10TRONG` và một môn kỳ 1; hoặc năm 2027–2028, một lớp DEMOFULL26, kỳ 2 (chưa phân công).
7. **Lớp ngừng hoạt động:** `DEMOFULL26-10DONG` để thử trạng thái khóa tạo phân công.
8. **Điểm danh:** chọn các ngày làm việc từ 02/11 đến 19/11/2026, có sáng/chiều. Ngày 19/11 thêm tiết 1 môn Toán. Có hiện diện, vắng có phép, vắng không phép, đi muộn và chưa ghi nhận. Phiên ngày 19/11 đang nhập; ngày trước đó đã chốt.
9. **Quyền giáo viên:** `demofull26_gv01` chủ nhiệm 10A1 năm 2026–2027 và dạy Toán; thử quyền điểm danh theo quy định của service hiện tại.

Điểm số và tổng kết được nạp để phục vụ phần học tập sau này; chưa có màn hình/API tương ứng trong bản ứng dụng đã kiểm tra. Tổng kết là dữ liệu minh họa, không phải kết quả tính tự động từ các điểm mẫu.

## Chạy lại

Script: `database/seed_demo_full.py`, cần thư viện Python `pymysql`.

```powershell
python -X utf8 database/seed_demo_full.py
python -X utf8 database/seed_demo_full.py --apply
```

Không có `--apply`: thực hiện trong transaction rồi rollback. Có `--apply`: kiểm tra liên kết rồi commit. Khi đã có dấu hoàn thành bộ dữ liệu, script thoát và không thêm trùng. Không xóa dấu này để chạy lại trên bộ dữ liệu đã được chỉnh sửa.

Script chỉ kết nối database local, không tắt khóa ngoại; kiểm tra xếp lớp/phân công trùng thời gian, học sinh thuộc lớp tại ngày điểm danh và môn/học kỳ/ngày của lần đánh giá. Báo cáo lần nạp nằm trong `database/demo_full_report.json`.
