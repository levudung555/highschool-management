> **Cập nhật 08/11/2026:** Phần xếp/chuyển lớp chờ duyệt dưới đây là tài liệu phiên bản cũ. Dùng [luồng trực tiếp hiện tại](XEP_LOP_TRUC_TIEP.md).

# Giao diện Bootstrap chạy local

## Mở giao diện
1. Mở Run > Edit Configurations trong IntelliJ.
2. Program arguments: --spring.profiles.active=local
3. Dừng và chạy lại HighschoolManagementApplication.
4. Mở http://localhost:8080/login.html, đăng nhập uc09_local_giaovu hoặc uc09_local_bgh với mật khẩu Test@12345 (không mở file://). Xem DANG_NHAP.md.
Có thể chạy run-local.cmd tại thư mục gốc để bật profile local bằng Maven Wrapper. Dừng ứng dụng cũ đang giữ cổng 8080 trước khi chạy.
Không cần npm, Node, React hoặc frontend server. Spring Boot phục vụ file trong src/main/resources/static.
Bootstrap 5.3.8 và giấy phép MIT được lưu tại static/vendor/bootstrap, nên giao diện không phụ thuộc CDN lúc chạy.

## Các màn hình
- Lớp học: tìm theo tên, lọc năm học/khối/trạng thái, phân trang, thêm, sửa, xóa có xác nhận. Không có sĩ số tối đa; số trên bảng là sĩ số đã duyệt hôm nay.
- Danh sách lớp: chọn lớp/ngày, phân trang, mở lịch sử hoặc chuyển lớp của một học sinh.
- Yêu cầu & phê duyệt: tìm học sinh/mã/lớp, lọc chờ duyệt/đã duyệt/từ chối, tạo yêu cầu xếp/chuyển lớp, xem chi tiết và duyệt/từ chối có xác nhận.
- Lịch sử học sinh: tìm/chọn hồ sơ để xem timeline phân lớp, khoảng hiệu lực, lý do và trạng thái.

## Quy trình thử
1. Thêm một lớp hoặc chọn lớp hoạt động có sẵn.
2. Trong Yêu cầu & phê duyệt, chọn Xếp lớp, chọn học sinh, lớp và ngày. Nhấn Tạo yêu cầu.
3. Yêu cầu xuất hiện trong Chờ duyệt. Đăng xuất giáo vụ, đăng nhập BGH, nhấn Duyệt và xác nhận.
4. Sang Danh sách lớp, chọn lớp/ngày để thấy học sinh.
5. Nhấn biểu tượng chuyển lớp bên cạnh học sinh, chọn lớp khác cùng khối/năm học, ngày chuyển sau ngày bắt đầu và nhập lý do.
6. Duyệt yêu cầu chuyển; xem danh sách cũ/mới tại ngày chuyển và Lịch sử học sinh.
Học sinh và danh mục năm học/khối lấy từ dữ liệu của nhóm. Giao diện không tạo ngầm dữ liệu mẫu.

## API đọc bổ sung
GET /api/v1/classes/catalog: năm học, khối, ngày hiện tại.
GET /api/v1/classes?tuKhoa=&maNamHoc=&maKhoi=&trangThai=&trang=0&kichThuoc=8
GET /api/v1/enrollments/students?tuKhoa=&trang=0&kichThuoc=30
GET /api/v1/enrollments?tuKhoa=&trangThai=CHO_DUYET&trang=0&kichThuoc=8
Các API thuộc SchoolClassController/SchoolClassService/SchoolClassDao (UC08) và EnrollmentController/EnrollmentService/EnrollmentDao (UC09), kiểm tra quyền tại service. Không có SchoolUi hoặc API tổng quan. Tra cứu học sinh chỉ phục vụ chọn học sinh xếp/chuyển lớp, không triển khai CRUD hồ sơ học sinh. Các thao tác ghi dùng lại API UC08/UC09 đã có.
Biểu mẫu và bảng escape nội dung người dùng trước khi render HTML. Nút submit bị khóa trong lúc gửi; lỗi nghiệp vụ hiển thị ngay trong modal. Xóa lớp và duyệt/từ chối đều có xác nhận.

## Kết nối và lỗi
401: thiếu/sai/hết hạn/đã thu hồi token. Giao diện quay lại trang đăng nhập.
403: tài khoản thiếu quyền thao tác.
409: lớp có dữ liệu liên quan, trùng tên lớp, trùng thời gian, chuyển sai khối/năm học hoặc trạng thái thay đổi. Đọc thông báo trong hộp thoại.
Nếu trang vẫn cũ sau khi thay code: chạy lại app rồi Ctrl+F5 trên trình duyệt.
Profile local yêu cầu đăng nhập thật bằng BCrypt/JWT; danh tính được lấy từ tài khoản đang đăng nhập. Nút đăng xuất ở cuối thanh menu; đăng xuất thu hồi token trên server.

## Nguồn
Bootstrap chính thức: https://getbootstrap.com/docs/5.3/getting-started/introduction/
Bootstrap 5.3.8 / MIT: https://github.com/twbs/bootstrap/tree/v5.3.8
Mẫu JdbcTemplate và tham số SQL: https://docs.spring.io/spring-framework/reference/data-access/jdbc/core.html
HTML/CSS/JavaScript và các projection/API đọc cho Schoolspace được viết riêng cho project của nhóm; không phải giao diện sao chép từ repo Spring.

## Kiểm chứng giao diện
Đã kiểm tra trên Edge desktop 1440px và điện thoại 390px với H2 riêng: lọc/thêm/sửa/xóa lớp, tạo/chi tiết/duyệt yêu cầu, danh sách lớp, chuyển lớp và lịch sử hai khoảng thời gian, chặn xóa lớp có dữ liệu, điều hướng mobile. Không có JavaScript page error trong lượt chạy. Các thao tác thử trên trình duyệt không ghi dữ liệu học sinh/xếp lớp vào MySQL của người dùng.

Sau khi bỏ SchoolUi: đã chạy lại 27 kiểm thử H2 và kiểm tra trình duyệt cho CRUD/lọc lớp, xếp/duyệt, chuyển lớp, lịch sử và mobile. Giao diện không gọi /ui; các endpoint /ui/catalog và /ui/overview cũ trả 404 sau đăng nhập.
