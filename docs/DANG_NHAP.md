# Đăng nhập và phân quyền chạy local

## Chạy thử
1. Bật MySQL, dùng database hiện có của nhóm; không chạy schema test lên MySQL.
2. Dừng phiên ứng dụng cũ rồi chạy `run-local.cmd`, hoặc thêm Program arguments `--spring.profiles.active=local` trong IntelliJ và chạy lại.
3. Mở http://localhost:8080/login.html. Không mở file HTML trực tiếp.
4. Đăng nhập một trong hai tài khoản demo:

| Tên đăng nhập | Mật khẩu demo | Quyền |
| --- | --- | --- |
| uc09_local_giaovu | Test@12345 | Quản lý lớp, tạo yêu cầu, xem danh sách và lịch sử |
| uc09_local_bgh | Test@12345 | Các quyền trên, thêm duyệt/từ chối |

Script local chỉ tạo tài khoản demo còn thiếu; riêng tài khoản BGH demo cũ có marker `!LOCAL_DEMO_NO_PASSWORD!` được nâng cấp sang mật khẩu BCrypt. Không đặt lại mật khẩu đã thay đổi, không xóa hồ sơ/xếp lớp. Không có đăng ký hoặc quản lý tài khoản trong phạm vi này.

## Luồng và trách nhiệm từng tầng
`login.html → login.js → POST /api/v1/auth/login → AuthController → AuthServiceImpl → AuthDaoImpl → tai_khoan + vai_tro`.

1. Controller kiểm tra JSON có tên đăng nhập/mật khẩu và giới hạn độ dài.
2. Service tìm tài khoản theo tên (trim tên, giữ nguyên mật khẩu), so sánh BCrypt, kiểm tra HOAT_DONG và quyền được hỗ trợ. Tên không tồn tại, sai mật khẩu hoặc tài khoản khóa đều trả cùng lỗi 401.
3. Service cấp JWT HS256 có thời hạn 30 phút. `sub` là MaTK, không phải username; `ver` là TokenVersion. Response chỉ chứa token, thời hạn và hồ sơ công khai, không chứa hash mật khẩu.
4. Frontend giữ token trong sessionStorage của tab và gửi `Authorization: Bearer <token>` khi gọi API.
5. Spring Security xác minh chữ ký, issuer, audience, thời hạn; kiểm tra tài khoản còn hoạt động và TokenVersion khớp database. Quyền được đọc lại từ database mỗi request.
6. NguoiDungHienTai đọc SecurityContext đã xác thực; service UC08/UC09 kiểm tra quyền trước nghiệp vụ. Không nhận ID người duyệt hoặc vai trò từ JSON.
7. Frontend hiển thị họ tên/quyền và chỉ hiện nút duyệt/từ chối cho BGH/ADMIN. Backend vẫn chặn 403 nếu tự gửi API trái quyền.
8. Đăng xuất tăng TokenVersion, thu hồi tất cả token đã cấp trước đó của tài khoản; frontend xóa token và quay về login. Hết hạn/401 cũng quay về login.

NguoiDung là DTO hồ sơ trong AuthResponse; UserAccount là bản ghi nội bộ chứa hash, không trả ra API. ServletNguoiDungHienTai là adapter SecurityContext dùng chung mọi profile; đã bỏ adapter local tự nhận BGH.

## Kiểm tra Postman
POST http://localhost:8080/api/v1/auth/login — chọn No Auth, Body raw JSON:
```json
{"tenDangNhap":"uc09_local_giaovu","matKhau":"Test@12345"}
```
Kết quả 200: `accessToken`, `tokenType: Bearer`, `expiresAt`, `nguoiDung`.
Các API UC08/UC09 và GET `/api/v1/auth/me` chọn Authorization → Bearer Token, dán accessToken. Khi duyệt phải đăng nhập tài khoản BGH rồi dùng token BGH.
POST `/api/v1/auth/logout` với Bearer Token: trả 204; gọi `/me` lại với token cũ trả 401.
GET `/api/v1/auth/options` công khai chỉ cho biết đang bật local hay không.

## Cấu hình và giới hạn
Mặc định khóa ký ngẫu nhiên 32 byte ở mỗi lần khởi động; khởi động lại sẽ cần đăng nhập lại. Có thể đặt biến môi trường `JWT_SECRET_BASE64` là Base64 của ít nhất 32 byte ngẫu nhiên để giữ khóa ổn định. Không lưu khóa thật vào Git. Đây là đăng nhập tự cấp JWT cho project chạy local; không sử dụng Keycloak của MiniShop.

Không có refresh token: sau 30 phút đăng nhập lại. Mật khẩu dùng BCrypt, tối đa 72 byte UTF-8. Token trong sessionStorage có thể bị đọc nếu có XSS; tiếp tục escape dữ liệu khi render. Tài khoản demo và HTTP localhost chỉ phục vụ học/thử local; không mở công khai cấu hình demo.

401: thiếu/sai/hết hạn/đã thu hồi token, tài khoản bị khóa hoặc sai mật khẩu. 403: đã xác thực nhưng thiếu quyền. 400: dữ liệu không hợp lệ. Nếu vẫn dùng ứng dụng cũ đang chạy thì dừng/chạy lại và Ctrl+F5.

## Đối chiếu nguồn
- Spring Security Resource Server JWT: https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html — SecurityFilterChain, JwtDecoder, validator và JwtAuthenticationConverter.
- Mã nguồn Spring Security: https://github.com/spring-projects/spring-security/tree/main/oauth2/oauth2-jose/src/main/java/org/springframework/security/oauth2/jwt — NimbusJwtEncoder/Decoder và claims.
- Password storage: https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html — PasswordEncoder và BCrypt.
- Mã nguồn BCrypt: https://github.com/spring-projects/spring-security/blob/main/crypto/src/main/java/org/springframework/security/crypto/bcrypt/BCryptPasswordEncoder.java

Phần đăng nhập, TokenVersion, truy vấn tài khoản/quyền, DTO và giao diện được biên soạn cho project này dựa trên API/pattern Spring; không phải code nghiệp vụ sao chép nguyên bản từ Spring samples.

## Bài tập
Đăng nhập giáo vụ, tạo một yêu cầu; thử duyệt bằng token giáo vụ để thấy 403; đăng xuất, đăng nhập BGH và duyệt. Sau đó thử token cũ sau logout, tài khoản bị khóa và sai mật khẩu để hiểu khác nhau giữa 401 và 403.

## Kết quả kiểm tra
Đã đạt 27 kiểm thử tự động (6 auth, 18 UC09, 2 local/UI, 1 context) trên H2. Edge desktop/mobile: chuyển hướng login, sai mật khẩu, giáo vụ gửi yêu cầu nhưng không duyệt, BGH duyệt, đăng xuất thu hồi token, CRUD lớp, chuyển lớp và lịch sử đều đạt, không có JavaScript page error. Trên MySQL thật đã kiểm tra hai tài khoản demo đăng nhập, /me, API lớp, đăng xuất và token cũ bị từ chối. Các phép thử ghi xếp/chuyển lớp trên trình duyệt dùng H2 riêng.
