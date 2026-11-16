import { request, escapeHtml, formatDate } from "./records-api.js";

const $ = id => document.getElementById(id);
const endpoint = "/api/v1/teaching-assignments";
const state = { options: [], allowed: false, today: "", historySeq: 0, detailSeq: 0, loadSeq: 0, saving: false };
const modal = id => bootstrap.Modal.getOrCreateInstance($(id));
const selected = () => state.options.find(r => r.maLopMonHoc === Number($("ta-subject").value));
function message(id, text = "") { $(id).textContent = text; $(id).hidden = !text; }
function options(id, rows, value, label, placeholder) {
  const old = $(id).value;
  $(id).innerHTML = `<option value="">${escapeHtml(placeholder)}</option>` + rows.map(r =>
    `<option value="${Number(value(r))}">${escapeHtml(label(r))}</option>`).join("");
  if ([...$(id).options].some(o => o.value === old)) $(id).value = old;
}
const unique = (rows, key) => [...new Map(rows.map(r => [r[key], r])).values()];
function nextDay(day) {
  const date = new Date(day + "T12:00:00Z");
  date.setUTCDate(date.getUTCDate() + 1);
  return date.toISOString().slice(0, 10);
}
function updateCreate() {
  $("ta-create").disabled = !state.allowed || selected()?.trangThaiLop !== "HOAT_DONG";
}
export function mountTeaching() {
  document.querySelector(".side-nav").insertAdjacentHTML("beforeend",
    '<a href="#teaching" data-view="teaching"><span data-icon="users"></span>Phân công giảng dạy</a>');
  document.querySelector(".app-footer").insertAdjacentHTML("beforebegin", `
    <section class="view-panel" id="view-teaching" hidden>
      <div class="page-heading"><div><p class="eyebrow">UC10 · PHÂN CÔNG GIÁO VIÊN</p>
        <h1>Phân công giảng dạy</h1><p>Chọn lớp, môn học và học kỳ để phân công giáo viên, theo dõi lịch sử giảng dạy.</p></div>
        <button class="btn btn-primary" id="ta-create" disabled>+ Phân công giáo viên</button></div>
      <div class="alert alert-success" id="ta-success" role="status" hidden></div>
      <div class="alert alert-danger" id="ta-error" role="alert" hidden></div>
      <div class="card border-0 shadow-sm p-3 mb-3"><div class="row g-3 align-items-end">
        <div class="col-md-3"><label for="ta-year" class="form-label">Năm học</label><select id="ta-year" class="form-select"></select></div>
        <div class="col-md-3"><label for="ta-class" class="form-label">Lớp học</label><select id="ta-class" class="form-select"></select></div>
        <div class="col-md-4"><label for="ta-subject" class="form-label">Môn học · Học kỳ</label><select id="ta-subject" class="form-select"></select></div>
        <div class="col-md-2"><button class="btn btn-outline-primary w-100" id="ta-refresh">Tải lại</button></div>
      </div><p class="small text-secondary mt-3 mb-0" id="ta-period">Chỉ hiển thị môn học đã được thiết lập cho lớp và học kỳ.</p></div>
      <div class="card border-0 shadow-sm"><div class="p-3 border-bottom"><strong id="ta-summary">Lịch sử phân công</strong></div>
        <div class="table-responsive"><table class="table align-middle mb-0"><thead><tr>
          <th>Giáo viên</th><th>Từ ngày</th><th>Đến ngày</th><th>Hiệu lực</th><th>Người duyệt</th><th>Thao tác</th>
        </tr></thead><tbody id="ta-rows"></tbody></table></div>
      </div>
    </section>`);
  document.body.insertAdjacentHTML("beforeend", `
    <div class="modal fade" id="ta-modal" tabindex="-1" aria-labelledby="ta-title" aria-hidden="true"><div class="modal-dialog modal-dialog-centered"><div class="modal-content">
      <form id="ta-form"><div class="modal-header"><h2 class="modal-title fs-5" id="ta-title">Phân công giáo viên</h2><button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Đóng"></button></div>
        <div class="modal-body"><div class="alert alert-danger" id="ta-form-error" role="alert" hidden></div>
          <div class="bg-light rounded p-3 mb-3"><strong id="ta-context"></strong><p id="ta-form-period" class="small text-secondary mb-0 mt-1"></p></div>
          <input id="ta-input-subject" type="hidden">
          <label for="ta-teacher" class="form-label">Giáo viên đang công tác *</label><select class="form-select mb-3" id="ta-teacher" required></select>
          <div class="row g-3"><div class="col-sm-6"><label for="ta-start" class="form-label">Ngày bắt đầu *</label><input class="form-control" id="ta-start" type="date" required></div>
          <div class="col-sm-6"><label for="ta-end" class="form-label">Ngày kết thúc</label><input class="form-control" id="ta-end" type="date"></div></div>
          <p class="small text-secondary mt-2">Ngày kết thúc phải sau ngày bắt đầu. Để trống nếu chưa xác định; hiệu lực giới hạn đến cuối học kỳ.</p>
          <div class="alert alert-info mb-0">Phân công được duyệt trực tiếp. Không thể phân công trùng thời gian cho cùng lớp, môn và học kỳ.</div>
        </div><div class="modal-footer"><button type="button" class="btn btn-light" data-bs-dismiss="modal">Hủy</button><button class="btn btn-primary" type="submit" id="ta-save">Lưu phân công</button></div>
      </form>
    </div></div></div>
    <div class="modal fade" id="ta-detail-modal" tabindex="-1" aria-labelledby="ta-detail-title" aria-hidden="true"><div class="modal-dialog modal-dialog-centered"><div class="modal-content">
      <div class="modal-header"><h2 class="modal-title fs-5" id="ta-detail-title">Chi tiết phân công giảng dạy</h2><button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Đóng"></button></div>
      <div class="modal-body" id="ta-detail-body"></div>
    </div></div></div>`);
  $("ta-year").addEventListener("change", () => { filterClasses(); loadHistory(); });
  $("ta-class").addEventListener("change", () => { filterSubjects(); loadHistory(); });
  $("ta-subject").addEventListener("change", loadHistory);
  $("ta-refresh").addEventListener("click", loadTeaching);
  $("ta-create").addEventListener("click", openTeaching);
  $("ta-start").addEventListener("change", () => { $("ta-end").min = $("ta-start").value ? nextDay($("ta-start").value) : ""; });
  $("ta-form").addEventListener("submit", saveTeaching);
  $("ta-modal").addEventListener("hide.bs.modal", event => { if (state.saving) event.preventDefault(); });
  $("ta-rows").addEventListener("click", event => {
    const button = event.target.closest("[data-teaching-id]");
    if (button) showDetail(Number(button.dataset.teachingId));
  });
}
export function configureTeaching(user, catalog) {
  state.allowed = user.vaiTro.some(r => ["ADMIN", "BGH", "GIAO_VU"].includes(r));
  state.today = catalog.homNay;
  updateCreate();
}
function filterClasses() {
  const rows = state.options.filter(r => r.maNamHoc === Number($("ta-year").value));
  options("ta-class", unique(rows, "maLop"), r => r.maLop, r => r.tenLop, "Chọn lớp");
  filterSubjects();
}
function filterSubjects() {
  const rows = state.options.filter(r => r.maLop === Number($("ta-class").value));
  options("ta-subject", rows, r => r.maLopMonHoc, r => `${r.tenMonHoc} · Học kỳ ${r.soHocKy}`, "Chọn môn học và học kỳ");
  updateCreate();
}
export async function loadTeaching() {
  const seq = ++state.loadSeq;
  ++state.historySeq;
  message("ta-error");
  $("ta-create").disabled = true;
  $("ta-refresh").disabled = true;
  for (const id of ["ta-year", "ta-class", "ta-subject"]) $(id).disabled = true;
  $("ta-rows").innerHTML = '<tr><td colspan="6" class="text-center p-4">Đang tải danh sách lớp–môn–học kỳ…</td></tr>';
  try {
    if (!state.allowed) throw new Error("Bạn không có quyền xem phân công giảng dạy.");
    const rows = await request(`${endpoint}/options`);
    if (seq !== state.loadSeq) return;
    state.options = rows;
    options("ta-year", unique(rows, "maNamHoc"), r => r.maNamHoc, r => r.namHoc, "Chọn năm học");
    if (!$("ta-year").value) {
      const current = rows.find(r => r.batDau <= state.today && r.ketThuc >= state.today) || rows[0];
      $("ta-year").value = current?.maNamHoc || "";
    }
    filterClasses();
    await loadHistory();
    if (!rows.length) message("ta-error", "Chưa có lớp–môn–học kỳ. Cần thiết lập môn học cho lớp trước khi phân công.");
  } catch (err) {
    if (seq !== state.loadSeq) return;
    state.options = [];
    for (const id of ["ta-year", "ta-class", "ta-subject"]) $(id).innerHTML = '<option value="">Chưa tải được dữ liệu</option>';
    message("ta-error", err.message);
    $("ta-summary").textContent = "Lịch sử phân công";
    $("ta-rows").innerHTML = '<tr><td colspan="6" class="text-center p-4">Nhấn “Tải lại” để thử lại.</td></tr>';
  } finally {
    if (seq === state.loadSeq) {
      $("ta-refresh").disabled = false;
      for (const id of ["ta-year", "ta-class", "ta-subject"]) $(id).disabled = !state.allowed || !state.options.length;
      updateCreate();
    }
  }
}
function effectiveStatus(row) {
  if (row.trangThaiDuyet !== "DA_DUYET") return row.trangThaiDuyet === "TU_CHOI" ? "Đã từ chối" : "Chờ duyệt";
  const option = state.options.find(r => r.maLopMonHoc === row.maLopMonHoc);
  const end = row.denNgay || option?.ketThuc;
  if (end && end < state.today) return "Đã kết thúc";
  if (row.tuNgay > state.today) return "Sắp bắt đầu";
  return "Đang hiệu lực";
}
export async function loadHistory() {
  const seq = ++state.historySeq;
  const option = selected();
  message("ta-error");
  updateCreate();
  $("ta-summary").textContent = option ? `${option.tenLop} · ${option.tenMonHoc} · Học kỳ ${option.soHocKy}` : "Lịch sử phân công";
  $("ta-period").textContent = option ? `Học kỳ: ${formatDate(option.batDau)} – ${formatDate(option.ketThuc)}${option.trangThaiLop !== "HOAT_DONG" ? " · Lớp đã ngừng hoạt động, chỉ xem lịch sử." : ""}` : "Chọn năm học, lớp và môn–học kỳ để tiếp tục.";
  $("ta-rows").innerHTML = `<tr><td colspan="6" class="text-center text-secondary p-4">${option ? "Đang tải lịch sử…" : "Chọn lớp và môn–học kỳ để xem lịch sử hoặc tạo phân công."}</td></tr>`;
  if (!option || !state.allowed) return;
  try {
    const rows = await request(`${endpoint}?maLopMonHoc=${option.maLopMonHoc}`);
    if (seq !== state.historySeq) return;
    $("ta-rows").innerHTML = rows.length ? rows.map(row => `<tr>
      <td><strong>${escapeHtml(row.tenGiaoVien)}</strong><small class="d-block text-secondary">Phân công #${Number(row.maPhanCong)}</small></td>
      <td>${formatDate(row.tuNgay)}</td><td>${row.denNgay ? formatDate(row.denNgay) : "Đến cuối học kỳ"}</td>
      <td><span class="badge text-bg-light">${effectiveStatus(row)}</span></td><td>${escapeHtml(row.tenNguoiDuyet || "—")}</td>
      <td><button class="btn btn-sm btn-outline-primary" data-teaching-id="${Number(row.maPhanCong)}">Chi tiết</button></td>
    </tr>`).join("") : '<tr><td colspan="6" class="text-center p-4">Chưa có phân công. Nhấn “Phân công giáo viên” để tạo mới.</td></tr>';
  } catch (err) {
    if (seq !== state.historySeq) return;
    message("ta-error", err.message);
    $("ta-rows").innerHTML = '<tr><td colspan="6" class="text-center p-4">Không tải được lịch sử. Nhấn “Tải lại” để thử lại.</td></tr>';
  }
}
export async function openTeaching() {
  const option = selected();
  if (!state.allowed || option?.trangThaiLop !== "HOAT_DONG" || state.saving) return;
  $("ta-form").reset();
  message("ta-form-error");
  $("ta-input-subject").value = option.maLopMonHoc;
  $("ta-context").textContent = `${option.tenLop} · ${option.tenMonHoc} · Học kỳ ${option.soHocKy} · ${option.namHoc}`;
  $("ta-form-period").textContent = `Thời gian học kỳ: ${formatDate(option.batDau)} – ${formatDate(option.ketThuc)}`;
  $("ta-start").min = option.batDau;
  $("ta-start").max = $("ta-end").max = option.ketThuc;
  $("ta-start").value = state.today >= option.batDau && state.today <= option.ketThuc ? state.today : option.batDau;
  $("ta-end").min = nextDay($("ta-start").value);
  $("ta-teacher").innerHTML = '<option value="">Đang tải giáo viên…</option>';
  $("ta-save").disabled = true;
  modal("ta-modal").show();
  try {
    const teachers = (await request("/teacher")).filter(r => r.trangThai === "DANG_CONG_TAC");
    options("ta-teacher", teachers, r => r.maGV, r => `${r.hoTen} · ${r.maSoGV}`, "Chọn giáo viên");
    if (!teachers.length) throw new Error("Chưa có giáo viên đang công tác để phân công.");
    $("ta-save").disabled = false;
  } catch (err) { message("ta-form-error", err.message); }
}
async function saveTeaching(event) {
  event.preventDefault();
  if (state.saving || $("ta-save").disabled || !state.allowed || !$("ta-form").reportValidity()) return;
  const body = { maLopMonHoc: Number($("ta-input-subject").value), maGiaoVien: Number($("ta-teacher").value), tuNgay: $("ta-start").value, denNgay: $("ta-end").value || null };
  state.saving = true;
  $("ta-save").disabled = true;
  $("ta-save").textContent = "Đang lưu…";
  message("ta-form-error");
  try {
    await request(endpoint, { method: "POST", body: JSON.stringify(body) });
    state.saving = false;
    modal("ta-modal").hide();
    message("ta-success", "Đã phân công giảng dạy và duyệt trực tiếp thành công.");
    await loadHistory();
  } catch (err) { message("ta-form-error", err.message); }
  finally { state.saving = false; $("ta-save").disabled = false; $("ta-save").textContent = "Lưu phân công"; }
}
async function showDetail(id) {
  const seq = ++state.detailSeq;
  $("ta-detail-body").textContent = "Đang tải…";
  modal("ta-detail-modal").show();
  try {
    const row = await request(`${endpoint}/${id}`);
    if (seq !== state.detailSeq) return;
    const fields = [["Mã phân công", row.maPhanCong], ["Lớp", row.tenLop], ["Môn học", row.tenMonHoc], ["Học kỳ", row.soHocKy],
      ["Năm học", row.namHoc], ["Giáo viên", row.tenGiaoVien], ["Từ ngày", formatDate(row.tuNgay)],
      ["Đến ngày", row.denNgay ? formatDate(row.denNgay) : "Đến cuối học kỳ"], ["Hiệu lực", effectiveStatus(row)],
      ["Người duyệt", row.tenNguoiDuyet], ["Thời điểm duyệt", row.duyetLuc?.replace("T", " ")]];
    $("ta-detail-body").innerHTML = '<dl class="detail-grid">' + fields.map(([label, value]) =>
      `<div><dt>${escapeHtml(label)}</dt><dd>${escapeHtml(value ?? "—")}</dd></div>`).join("") + '</dl>';
  } catch (err) { if (seq === state.detailSeq) $("ta-detail-body").textContent = err.message; }
}
