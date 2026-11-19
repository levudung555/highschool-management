> **Cập nhật 08/11/2026:** Phần xếp/chuyển lớp chờ duyệt dưới đây là tài liệu phiên bản cũ. Dùng [luồng trực tiếp hiện tại](XEP_LOP_TRUC_TIEP.md).

# Giao diện Bootstrap UC05–UC09

## Chạy thử
MySQL phải có schema đầy đủ của nhóm (gồm hồ sơ, lớp, năm học, xếp lớp và tài khoản). Không chạy lại schema/seed lên database đang có dữ liệu để thử giao diện.

Chạy `run-local.cmd`, hoặc chạy ứng dụng trong IntelliJ với Program arguments `--spring.profiles.active=local`. Dừng ứng dụng cũ trước, sau đó mở http://localhost:8080/login.html và Ctrl+F5 nếu còn giao diện cũ.

Giáo vụ: `uc09_local_giaovu` / `Test@12345`. BGH: `uc09_local_bgh` / `Test@12345`.

## Phạm vi màn hình
- Trang chủ/dashboard: số lượng học sinh, giáo viên, lớp, yêu cầu chờ; lối tắt và quy trình học vụ. Số liệu lấy từ API nghiệp vụ, không có module SchoolUi.
- UC05: thêm/sửa/xem/xóa có điều kiện học sinh; CRUD người giám hộ; trong Chi tiết học sinh có xem, thêm/cập nhật quan hệ và gỡ liên kết người giám hộ.
- UC06: tìm đúng mã học sinh, tìm tên chứa từ khóa, kết hợp bộ lọc lớp/khối/năm học. Bộ lọc xét phân lớp DA_DUYET còn hiệu lực hôm nay. Không chọn bộ lọc thì bao gồm học sinh chưa có lớp. Danh sách trả về từ server, chia trang 10 dòng ở trình duyệt.
- UC07: CRUD giáo viên; tìm theo tên/mã/chuyên môn/số điện thoại, lọc trạng thái; thao tác Ngừng công tác giữ lại hồ sơ. Tìm/lọc giáo viên thực hiện ở trình duyệt trên danh sách từ API.
- UC08: CRUD lớp, lọc và phân trang tại server, sĩ số thực tế, chỉ xóa lớp chưa có dữ liệu liên quan.
- UC09: xếp lớp, chuyển lớp chờ duyệt, phê duyệt/từ chối theo quyền; xem danh sách theo ngày và lịch sử. Từ Chi tiết học sinh có thể mở lịch sử trực tiếp.

## Tổ chức code để đọc
- `static/index.html`: khung ứng dụng, menu, màn hình và modal UC08/UC09. Đã định dạng xuống dòng, thụt lề.
- `static/js/school.js`: xử lý UC08/UC09 và điều hướng chung; mục khởi tạo gọi configureRecords để chia sẻ danh mục và người dùng.
- `static/js/records-templates.js`: khai báo từng loại hồ sơ, các trường biểu mẫu, HTML trang chủ, bảng, bộ lọc và modal UC05–UC07. Mỗi trường có tên, nhãn, kiểu, bắt buộc và giới hạn độ dài.
- `static/js/records.js`: chia 6 khối có chú thích: trạng thái/khởi tạo; dashboard; tìm kiếm/phân trang; thêm/sửa; chi tiết/giám hộ; gắn sự kiện.
- `static/js/records-api.js`: gọi API có Bearer Token, xử lý lỗi, escape HTML, định dạng ngày và query string.
- `static/js/auth.js`, `login.js`, `login.html`: đăng nhập, phiên và đăng xuất.
- `static/css/records.css`: 4 khối: dashboard; bộ lọc; hồ sơ/giám hộ; responsive. CSS cũ cũng đã định dạng dễ đọc.
- `static/vendor/bootstrap`: Bootstrap 5.3.8 kèm giấy phép, không cần CDN hoặc npm khi chạy.

## UI/UX áp dụng
Menu nhất quán, phân biệt trạng thái hiện tại, tiêu đề và hành động chính rõ ràng. Nhãn form luôn hiển thị; trường bắt buộc có dấu *, kiểm tra email/ngày sinh/số điện thoại trước khi gửi. Backend tiếp tục kiểm tra cuối cùng.

Nút lưu bị khóa trong khi gửi, lỗi hiển thị trong modal, thông báo thành công dùng toast. Danh sách có trạng thái tải/trống/lỗi, nút phân trang có nhãn hỗ trợ truy cập. Xóa/ngừng công tác cần xác nhận; các thao tác không đóng modal khi thất bại. Nút Đặt lại khôi phục bộ lọc.

Trên điện thoại menu thu gọn, form một cột, bảng cuộn ngang trong vùng bảng. Không ghép dữ liệu API trực tiếp vào HTML nếu chưa escape.

## API được sử dụng
- GET/POST `/student`, PUT `/student/{maHS}`, DELETE `/student?maHS=...`.
- GET `/student/search?maSoHS=&hoTen=&maLop=&maKhoi=&maNamHoc=`.
- GET/POST `/teacher`, GET/PUT `/teacher/{maGV}`, DELETE `/teacher?maGV=...`.
- GET/POST `/guardian`, GET/PUT `/guardian/{maNGH}`, DELETE `/guardian?maNGH=...`.
- GET `/guardian/student/{maHS}`, POST `/guardian/link`, DELETE `/guardian/unlink?maHS=&maNGH=`.
- UC08/UC09 tiếp tục dùng `/api/v1/classes` và `/api/v1/enrollments`, không gọi `/ui`.

Các endpoint hồ sơ yêu cầu token và service kiểm tra ADMIN/BGH/GIAO_VU. Chỉ ADMIN/BGH duyệt/từ chối yêu cầu phân lớp.

## Điều chỉnh backend để giao diện chạy đúng
Sửa mã học sinh tự sinh đọc từ MaSoHS; lưu cập nhật học sinh bằng DAO; tránh báo trùng chính email/số điện thoại của hồ sơ đang sửa; thống nhất tên JSON maSoHS/hoTen/email; đọc an toàn ngày tiếp nhận null trong dữ liệu cũ. Bổ sung bộ lọc UC06 tại SQL.

JdbcConnect dùng TransactionAwareDataSourceProxy để các DAO JDBC của nhánh mới dùng chung DataSource và transaction với Spring, thay vì tự mở kết nối DriverManager ngoài transaction. Không thêm bảng nghiệp vụ hoặc chạy migration tự động trong công việc này.

## Kịch bản demo
1. Đăng nhập giáo vụ, xem dashboard, thêm người giám hộ.
2. Thêm học sinh; sửa tên; tìm lại theo mã/tên. Mở Chi tiết, liên kết người giám hộ, cập nhật quan hệ, xem lịch sử.
3. Thêm giáo viên; tìm chuyên môn; chuyển Ngừng công tác. Thử xóa hồ sơ demo chưa có dữ liệu liên quan.
4. Thêm lớp; xếp học sinh vào lớp, đăng xuất giáo vụ và đăng nhập BGH để duyệt.
5. Xem danh sách theo ngày, gửi/duyệt chuyển lớp, đối chiếu lịch sử. Trở lại UC06 lọc lớp để kiểm tra.

## Nguồn và phần biên soạn
Bootstrap: https://getbootstrap.com/docs/5.3/getting-started/introduction/ và https://github.com/twbs/bootstrap/tree/v5.3.8 . Spring JDBC transaction-aware connections: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/jdbc/datasource/TransactionAwareDataSourceProxy.html .

Giao diện và logic kết nối màn hình được viết riêng cho project, không phải giao diện mẫu nguyên bản của Spring. Không cần build frontend khi chạy local.

## Kiểm chứng
Đã kiểm tra 30 kiểm thử H2: 6 đăng nhập, 18 UC09, 2 local, 3 hồ sơ và 1 context. Trình duyệt Edge desktop/mobile đã chạy luồng hồ sơ/giám hộ/giáo viên, CRUD lớp, xếp/duyệt, chuyển lớp và lịch sử; không có JavaScript page error. Các API đọc hồ sơ và lọc UC06 trên MySQL thật trả 200 sau khi xử lý dữ liệu ngày tiếp nhận trống. Các thao tác ghi thử nghiệm trên trình duyệt dùng H2 riêng.
