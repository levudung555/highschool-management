import { escapeHtml } from "./records-api.js";

// Khai báo trường của từng biểu mẫu tại một nơi; không dùng tên cột SQL làm nhãn.
export const profiles = {
  students: {
    title: "Hồ sơ học sinh",
    singular: "học sinh",
    code: "UC05 · UC06",
    endpoint: "/student",
    id: "maHS",
    description: "Quản lý hồ sơ, tra cứu phân lớp và kết nối người giám hộ.",
    fields: [
      ["hoTen", "Họ và tên", "text", true, 100],
      ["ngaySinh", "Ngày sinh", "date", true],
      [
        "gioiTinh",
        "Giới tính",
        "select",
        true,
        [
          ["NAM", "Nam"],
          ["NU", "Nữ"],
        ],
      ],
      ["sdt", "Số điện thoại", "tel", false, 20],
      ["email", "Email", "email", false, 100],
      ["diaChi", "Địa chỉ", "text", false, 255],
    ],
  },
  teachers: {
    title: "Hồ sơ giáo viên",
    singular: "giáo viên",
    code: "UC07",
    endpoint: "/teacher",
    id: "maGV",
    description:
      "Tra cứu giáo viên, cập nhật chuyên môn và tình trạng công tác.",
    fields: [
      ["hoTen", "Họ và tên", "text", true, 150],
      ["ngaySinh", "Ngày sinh", "date", true],
      ["sdt", "Số điện thoại", "tel", true, 20],
      ["email", "Email", "email", false, 254],
      ["chuyenMon", "Chuyên môn", "text", false, 150],
      [
        "trangThai",
        "Tình trạng công tác",
        "select",
        true,
        [
          ["DANG_CONG_TAC", "Đang công tác"],
          ["NGUNG_CONG_TAC", "Ngừng công tác"],
        ],
      ],
    ],
  },
  guardians: {
    title: "Người giám hộ",
    singular: "người giám hộ",
    code: "UC05",
    endpoint: "/guardian",
    id: "maNGH",
    description: "Thông tin liên hệ dùng chung cho hồ sơ học sinh.",
    fields: [
      ["hoTen", "Họ và tên", "text", true, 150],
      ["sdt", "Số điện thoại", "tel", true, 20],
      ["email", "Email", "email", false, 254],
      ["diaChi", "Địa chỉ", "text", false, 500],
    ],
  },
};

function field([name, label, type, required, extra]) {
  const id = `profile-${name}`;
  const attributes = `id="${id}" name="${name}" ${required ? "required" : ""}`;
  const control =
    type === "select"
      ? `<select class="form-select" ${attributes}>${extra.map(([value, text]) => `<option value="${value}">${text}</option>`).join("")}</select>`
      : `<input class="form-control" ${attributes} type="${type}" ${extra ? `maxlength="${extra}"` : ""} ${type === "date" ? `max="${new Date().toLocaleDateString("en-CA")}"` : ""} ${type === "tel" ? 'pattern="[+0-9 ().-]{8,20}" title="Nhập số điện thoại từ 8 đến 20 ký tự"' : ""}>`;
  return `<div class="col-md-6"><label class="form-label" for="${id}">${label}${required ? ' <span class="text-danger">*</span>' : ""}</label>${control}</div>`;
}

export function profileFields(type) {
  return profiles[type].fields.map(field).join("");
}

export function profilePage(type) {
  const config = profiles[type];
  return `
    <section id="view-${type}" class="view-panel" hidden>
      <div class="page-heading">
        <div><div class="eyebrow">${config.code} · QUẢN LÝ HỒ SƠ</div><h1>${config.title}</h1><p>${config.description}</p></div>
        <button class="btn btn-primary" data-profile-add="${type}">＋ Thêm ${config.singular}</button>
      </div>
      <div class="section-card">
        <form class="profile-filters filter-bar" id="${type}-filters" data-profile-filters="${type}">
          <div class="profile-search"><label class="form-label" for="${type}-keyword">${type === "students" ? "Họ tên học sinh" : "Tìm kiếm"}</label><input class="form-control" id="${type}-keyword" name="hoTen" placeholder="Nhập họ tên${type === "teachers" ? ", mã hoặc chuyên môn" : ""}…" maxlength="100"></div>
          ${
            type === "students"
              ? `
            <div><label class="form-label" for="student-code">Mã học sinh</label><input class="form-control" id="student-code" name="maSoHS" placeholder="Ví dụ: HS0001" maxlength="30"></div>
            <div><label class="form-label" for="student-year">Năm học</label><select class="form-select" id="student-year" name="maNamHoc"></select></div>
            <div><label class="form-label" for="student-grade">Khối</label><select class="form-select" id="student-grade" name="maKhoi"></select></div>
            <div><label class="form-label" for="student-class">Lớp</label><select class="form-select" id="student-class" name="maLop"></select></div>
          `
              : ""
          }
          ${type === "teachers" ? '<div><label class="form-label" for="teacher-status">Tình trạng</label><select id="teacher-status" name="trangThai" class="form-select"><option value="">Tất cả</option><option value="DANG_CONG_TAC">Đang công tác</option><option value="NGUNG_CONG_TAC">Ngừng công tác</option></select></div>' : ""}
          <button class="btn btn-primary" type="submit">Tìm kiếm</button>
          <button class="btn btn-light" type="reset">Đặt lại</button>
        </form>
        ${type === "students" ? '<p class="filter-help">Bộ lọc lớp/khối/năm học xét phân lớp đã duyệt còn hiệu lực hôm nay.</p>' : ""}
        <div class="profile-feedback" id="${type}-feedback" role="status" aria-live="polite"></div>
        <div class="table-responsive"><table class="table app-table">
          <thead><tr><th>${type === "guardians" ? "Mã" : "Mã hồ sơ"}</th><th>Họ và tên</th><th>${type === "guardians" ? "Địa chỉ" : "Ngày sinh"}</th><th>Liên hệ</th><th>${type === "teachers" ? "Chuyên môn / Trạng thái" : type === "students" ? "Giới tính" : "Email"}</th><th class="text-end">Thao tác</th></tr></thead>
          <tbody id="${type}-rows"></tbody>
        </table></div>
        <div class="table-footer" id="${type}-pager"></div>
      </div>
    </section>`;
}

export const homePage = `
  <section id="view-home" class="view-panel" hidden>
    <div class="page-heading"><div><div class="eyebrow">KHÔNG GIAN ĐIỀU HÀNH</div><h1>Chào ngày mới, <span id="home-name">thầy cô</span>.</h1><p>Hồ sơ rõ ràng. Phân lớp liền mạch. Mọi công việc ở một nơi.</p></div><a href="#students" class="btn btn-primary">Tra cứu học sinh →</a></div>
    <div id="home-error" class="alert alert-danger d-none" role="alert"></div>
    <div class="stats-grid" id="home-stats" aria-live="polite"></div>
    <div class="home-layout">
      <article class="section-card home-work"><div class="eyebrow">BẮT ĐẦU TỪ ĐÂU?</div><h2>Công việc thường ngày</h2>
        <a class="work-link" href="#students"><span class="work-number">01</span><div><strong>Hồ sơ & tìm kiếm học sinh</strong><small>Thêm hồ sơ, lọc lớp/khối/năm học, quản lý người giám hộ</small></div><span>↗</span></a>
        <a class="work-link" href="#teachers"><span class="work-number">02</span><div><strong>Đội ngũ giáo viên</strong><small>Cập nhật chuyên môn và tình trạng công tác</small></div><span>↗</span></a>
        <a class="work-link" href="#classes"><span class="work-number">03</span><div><strong>Tổ chức lớp học</strong><small>Quản lý lớp, danh sách và sĩ số thực tế</small></div><span>↗</span></a>
        <a class="work-link" href="#requests"><span class="work-number">04</span><div><strong>Xếp lớp & phê duyệt</strong><small>Tạo yêu cầu, duyệt chuyển lớp và lưu lịch sử</small></div><span>↗</span></a>
      </article>
      <article class="section-card home-guide"><span class="eyebrow">QUY TRÌNH HỌC VỤ</span><h2>Từ hồ sơ đến lớp học</h2><ol><li><strong>Tiếp nhận hồ sơ</strong><p>Nhập thông tin học sinh và kết nối người giám hộ.</p></li><li><strong>Gửi yêu cầu xếp lớp</strong><p>Chọn lớp, năm học và ngày bắt đầu phù hợp.</p></li><li><strong>Phê duyệt & theo dõi</strong><p>BGH/Admin duyệt; danh sách lớp cập nhật theo ngày hiệu lực.</p></li></ol><a class="btn btn-outline-primary" href="#requests">Mở yêu cầu & phê duyệt →</a></article>
    </div>
  </section>`;

export const profileModals = `
  <!-- Thêm / sửa hồ sơ: nội dung trường được khai báo trong profiles. -->
  <div class="modal fade" id="profileModal" tabindex="-1" aria-labelledby="profile-title" aria-hidden="true"><div class="modal-dialog modal-lg modal-dialog-centered modal-dialog-scrollable"><div class="modal-content">
    <form id="profile-form"><div class="modal-header"><h2 class="modal-title fs-5" id="profile-title"></h2><button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Đóng"></button></div>
      <div class="modal-body"><div class="alert alert-danger d-none" id="profile-error" role="alert"></div><p class="small text-secondary">Các trường có dấu <span class="text-danger">*</span> là bắt buộc. Mã hồ sơ được hệ thống cấp.</p><div class="row g-3" id="profile-fields"></div></div>
      <div class="modal-footer"><button type="button" class="btn btn-light" data-bs-dismiss="modal">Hủy</button><button type="submit" class="btn btn-primary" id="profile-save">Lưu hồ sơ</button></div>
    </form></div></div></div>
  <!-- Chi tiết học sinh và liên kết người giám hộ. -->
  <div class="modal fade" id="profileDetailModal" tabindex="-1" aria-labelledby="profile-detail-title" aria-hidden="true"><div class="modal-dialog modal-lg modal-dialog-centered modal-dialog-scrollable"><div class="modal-content">
    <div class="modal-header"><h2 class="modal-title fs-5" id="profile-detail-title">Chi tiết hồ sơ</h2><button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Đóng"></button></div><div class="modal-body" id="profile-detail-body"></div><div class="modal-footer"><button class="btn btn-light" data-bs-dismiss="modal">Đóng</button></div>
  </div></div></div>
  <!-- Xác nhận thao tác xóa / ngừng công tác / gỡ liên kết. -->
  <div class="modal fade" id="profileConfirmModal" tabindex="-1" aria-labelledby="profile-confirm-title" aria-hidden="true"><div class="modal-dialog modal-dialog-centered"><div class="modal-content"><div class="modal-header"><h2 id="profile-confirm-title" class="modal-title fs-5">Xác nhận thao tác</h2><button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Đóng"></button></div><div class="modal-body"><p id="profile-confirm-message"></p><div class="alert alert-danger d-none" id="profile-confirm-error" role="alert"></div></div><div class="modal-footer"><button class="btn btn-light" data-bs-dismiss="modal">Hủy</button><button class="btn btn-danger" id="profile-confirm-button">Xác nhận</button></div></div></div></div>`;
