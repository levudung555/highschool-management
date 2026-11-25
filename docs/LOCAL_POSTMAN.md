> **Cập nhật 08/11/2026:** Phần xếp/chuyển lớp chờ duyệt dưới đây là tài liệu phiên bản cũ. Dùng [luồng trực tiếp hiện tại](XEP_LOP_TRUC_TIEP.md).

# Chạy UC09 local bằng Postman

Trong IntelliJ mở Run > Edit Configurations > HighschoolManagementApplication.
Thêm Program arguments: --spring.profiles.active=local
Chạy lại ứng dụng. Nếu nhập ở Environment variables thay vì Program arguments, dùng SPRING_PROFILES_ACTIVE=local.

Profile local chỉ lắng nghe 127.0.0.1:8080 và vẫn yêu cầu đăng nhập.
POST /api/v1/auth/login chọn No Auth, JSON {"tenDangNhap":"uc09_local_giaovu","matKhau":"Test@12345"}.
Copy accessToken, chọn Authorization → Bearer Token cho các API dưới đây.
Khi duyệt/từ chối, đăng nhập uc09_local_bgh / Test@12345 rồi dùng token BGH.
Script local tạo hai tài khoản demo với mật khẩu BCrypt và bảng phụ nếu thiếu. Tài khoản BGH demo cũ chưa có mật khẩu được nâng cấp; không đặt lại mật khẩu đã thay đổi. Không xóa dữ liệu xếp lớp/học sinh.
Xem DANG_NHAP.md để kiểm tra /me, logout và lỗi 401/403. Không mở cấu hình demo ra public.

Dùng ID thật. Hai lớp phải cùng khối/năm học, hoạt động. Chọn học sinh không có phân lớp chồng thời gian.
Ngày xếp từ hôm nay; ngày chuyển sau ngày xếp và trong năm học.

1. POST http://localhost:8080/api/v1/enrollments
Body raw JSON: {"maHocSinh":1,"maLop":1,"tuNgay":"2026-11-04"}
Mong đợi 201 CHO_DUYET, ghi lại maXepLop.
2. PUT http://localhost:8080/api/v1/enrollments/MA_XEP_LOP/approval
Không body. Mong đợi 200 DA_DUYET.
3. GET http://localhost:8080/api/v1/classes/1/students?ngay=2026-11-04
Mong đợi 200, có học sinh vừa duyệt.
4. POST http://localhost:8080/api/v1/enrollments/transfers
Body raw JSON: {"maHocSinh":1,"maLopDich":2,"ngayChuyen":"2026-11-05","lyDo":"Demo chuyển lớp"}
Mong đợi 201 CHO_DUYET, ghi lại mã yêu cầu mới.
5. PUT http://localhost:8080/api/v1/enrollments/MA_YEU_CAU_MOI/approval
Mong đợi 200, đóng phân lớp cũ và duyệt phân lớp mới cùng transaction.
6. GET http://localhost:8080/api/v1/students/1/enrollments
Có lịch sử nguồn/đích; dùng GET classes/{id}/students?ngay=2026-11-05 để đối chiếu.
Từ chối một yêu cầu chờ khác: PUT /api/v1/enrollments/{id}/rejection.

Không gửi maTaiKhoanDuyet trong JSON. Profile local tự lấy account server để lưu FK và thời điểm duyệt.
