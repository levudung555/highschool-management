import { request, escapeHtml, formatDate } from "./records-api.js";

const $ = id => document.getElementById(id);
const endpoint = "/api/v1/homeroom-assignments";
const state = { classes: [], catalog: null, allowed: false, sequence: 0, detailSequence: 0, teachers: [] };
const modal = id => bootstrap.Modal.getOrCreateInstance($(id));
const nextDay = day => {
  const value = new Date(day + "T12:00:00Z");
  value.setUTCDate(value.getUTCDate() + 1);
  return value.toISOString().slice(0, 10);
};
function options(id, rows, value, label, placeholder) {
  const old = $(id).value;
  $(id).innerHTML = `<option value="">${escapeHtml(placeholder)}</option>` + rows.map(row =>
    `<option value="${Number(value(row))}">${escapeHtml(label(row))}</option>`).join("");
  if ([...$(id).options].some(option => option.value === old)) $(id).value = old;
}
function error(id, message = "") {
  $(id).textContent = message;
  $(id).hidden = !message;
}

export function mountHomeroom() {
  document.querySelector(".side-nav").insertAdjacentHTML("beforeend",
    '<a href="#homeroom" data-view="homeroom"><span data-icon="shield"></span>Phân công chủ nhiệm</a>');
  document.querySelector(".app-footer").insertAdjacentHTML("beforebegin", `
    <section class="view-panel" id="view-homeroom" hidden>
      <div class="page-heading"><div><p class="eyebrow">UC10 · PHÂN CÔNG GIÁO VIÊN</p>
        <h1>Phân công chủ nhiệm</h1><p>Theo dõi giáo viên phụ trách và lịch sử chủ nhiệm của từng lớp.</p></div>
        <button class="btn btn-primary" id="hr-create" disabled>+ Phân công chủ nhiệm</button></div>
      <div class="alert alert-success" id="hr-success" role="status" hidden></div>
      <div class="alert alert-danger" id="hr-error" role="alert" hidden></div>
      <div class="card border-0 shadow-sm p-3 mb-3"><div class="row g-3 align-items-end">
        <div class="col-md-4"><label class="form-label" for="hr-year">Năm học</label><select class="form-select" id="hr-year"></select></div>
        <div class="col-md-5"><label class="form-label" for="hr-class">Lớp học</label><select class="form-select" id="hr-class"></select></div>
        <div class="col-md-3"><button class="btn btn-outline-primary w-100" id="hr-refresh">Tải lại lịch sử</button></div>
      </div></div>
      <div class="card border-0 shadow-sm"><div class="p-3 border-bottom"><strong id="hr-summary">Chọn lớp để xem lịch sử</strong></div>
        <div class="table-responsive"><table class="table align-middle mb-0"><thead><tr>
          <th>Giáo viên</th><th>Từ ngày</th><th>Đến ngày</th><th>Hiệu lực</th><th>Người duyệt</th><th>Thao tác</th>
        </tr></thead><tbody id="hr-rows"></tbody></table></div>
      </div>
    </section>`);
  document.body.insertAdjacentHTML("beforeend", `
    <div class="modal fade" id="hr-modal" tabindex="-1" aria-labelledby="hr-title" aria-hidden="true"><div class="modal-dialog modal-dialog-centered"><div class="modal-content">
      <form id="hr-form"><div class="modal-header"><h2 class="modal-title fs-5" id="hr-title">Phân công chủ nhiệm</h2><button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Đóng"></button></div>
      <div class="modal-body"><div class="alert alert-danger" id="hr-form-error" role="alert" hidden></div>
        <label for="hr-input-class" class="form-label">Lớp học *</label><select id="hr-input-class" class="form-select mb-3" required></select>
        <p id="hr-period" class="small text-secondary"></p>
        <label for="hr-teacher" class="form-label">Giáo viên đang công tác *</label><select id="hr-teacher" class="form-select mb-3" required></select>
        <div class="row g-3"><div class="col-sm-6"><label for="hr-start" class="form-label">Ngày bắt đầu *</label><input id="hr-start" type="date" class="form-control" required></div>
        <div class="col-sm-6"><label for="hr-end" class="form-label">Ngày kết thúc</label><input id="hr-end" type="date" class="form-control"></div></div>
        <p class="small text-secondary mt-2">Để trống ngày kết thúc nếu chưa xác định; hiệu lực được giới hạn trong năm học của lớp.</p>
        <div class="alert alert-info mb-0">Phân công được duyệt trực tiếp. Hệ thống sẽ từ chối nếu lớp đã có chủ nhiệm trùng thời gian.</div>
      </div><div class="modal-footer"><button type="button" class="btn btn-light" data-bs-dismiss="modal">Hủy</button><button type="submit" class="btn btn-primary" id="hr-save">Lưu phân công</button></div></form>
    </div></div></div>
    <div class="modal fade" id="hr-detail-modal" tabindex="-1" aria-labelledby="hr-detail-title" aria-hidden="true"><div class="modal-dialog modal-dialog-centered"><div class="modal-content">
      <div class="modal-header"><h2 id="hr-detail-title" class="modal-title fs-5">Chi tiết phân công</h2><button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Đóng"></button></div>
      <div class="modal-body" id="hr-detail-body"></div>
    </div></div></div>`);
  $("hr-year").addEventListener("change", () => { filterClasses(); loadHomeroom(); });
  $("hr-class").addEventListener("change", loadHomeroom);
  $("hr-refresh").addEventListener("click", loadHomeroom);
  $("hr-create").addEventListener("click", openHomeroom);
  $("hr-input-class").addEventListener("change", updateDates);
  $("hr-start").addEventListener("change", updateDates);
  $("hr-form").addEventListener("submit", saveHomeroom);
  $("hr-rows").addEventListener("click", event => {
    const button = event.target.closest("[data-homeroom-id]");
    if (button) showDetail(Number(button.dataset.homeroomId));
  });
}

export function configureHomeroom(user, catalog, classes) {
  state.catalog = catalog;
  state.classes = classes;
  state.allowed = user.vaiTro.some(role => ["ADMIN", "BGH", "GIAO_VU"].includes(role));
  $("hr-create").disabled = !state.allowed;
  options("hr-year", catalog.namHoc, y => y.maNamHoc, y => y.ten, "Tất cả năm học");
  filterClasses();
}
function filterClasses() {
  const year = $("hr-year").value;
  options("hr-class", state.classes.filter(c => !year || c.maNamHoc === Number(year)),
    c => c.maLop, c => `${c.tenLop} · ${c.namHoc}`, "Chọn lớp học");
}
function effectiveStatus(row) {
  if (row.trangThaiDuyet !== "DA_DUYET") return "Chưa được duyệt";
  const cls = state.classes.find(c => c.maLop === row.maLop);
  const year = state.catalog.namHoc.find(y => y.maNamHoc === cls?.maNamHoc);
  const end = row.denNgay && year ? (row.denNgay < year.ketThuc ? row.denNgay : year.ketThuc) : row.denNgay || year?.ketThuc;
  if (end && end < state.catalog.homNay) return "Đã kết thúc";
  if (row.tuNgay > state.catalog.homNay) return "Sắp bắt đầu";
  return "Đang hiệu lực";
}
export async function loadHomeroom() {
  const seq = ++state.sequence;
  const id = $("hr-class").value;
  error("hr-error");
  $("hr-summary").textContent = "Chọn lớp để xem lịch sử";
  $("hr-rows").innerHTML = `<tr><td colspan="6" class="text-center p-4 text-secondary">${id ? "Đang tải lịch sử…" : "Chọn năm học và lớp để xem các lần phân công chủ nhiệm."}</td></tr>`;
  if (!id || !state.catalog) return;
  try {
    const rows = await request(`${endpoint}?maLop=${id}`);
    if (seq !== state.sequence) return;
    const cls = state.classes.find(c => c.maLop === Number(id));
    $("hr-summary").textContent = `${cls?.tenLop || "Lớp"} · ${rows.length} phân công`;
    $("hr-rows").innerHTML = rows.length ? rows.map(row => `<tr>
      <td><strong>${escapeHtml(row.tenGiaoVien)}</strong><small class="d-block text-secondary">Phân công #${Number(row.maPhanCong)}</small></td>
      <td>${formatDate(row.tuNgay)}</td><td>${row.denNgay ? formatDate(row.denNgay) : "Chưa xác định"}</td>
      <td><span class="badge text-bg-light">${effectiveStatus(row)}</span></td><td>${escapeHtml(row.tenNguoiDuyet || "—")}</td>
      <td><button class="btn btn-outline-primary btn-sm" data-homeroom-id="${Number(row.maPhanCong)}">Chi tiết</button></td>
    </tr>`).join("") : '<tr><td colspan="6" class="text-center p-4">Lớp chưa có phân công chủ nhiệm được duyệt.</td></tr>';
  } catch (err) {
    if (seq !== state.sequence) return;
    error("hr-error", err.message);
    $("hr-rows").innerHTML = '<tr><td colspan="6" class="text-center p-4">Chưa tải được lịch sử. Nhấn “Tải lại lịch sử” để thử lại.</td></tr>';
  }
}
function updateDates() {
  const cls = state.classes.find(c => c.maLop === Number($("hr-input-class").value));
  const year = state.catalog.namHoc.find(y => y.maNamHoc === cls?.maNamHoc);
  const start = $("hr-start"), end = $("hr-end");
  start.min = year && year.batDau > state.catalog.homNay ? year.batDau : state.catalog.homNay;
  start.max = end.max = year?.ketThuc || "";
  if (!start.value || start.value < start.min || (start.max && start.value > start.max)) start.value = start.min;
  // Schema hiện tại yêu cầu DenNgay > TuNgay.
  end.min = nextDay(start.value);
  $("hr-period").textContent = year ? `Năm học: ${formatDate(year.batDau)} – ${formatDate(year.ketThuc)}. Ngày kết thúc phải sau ngày bắt đầu.` : "Chọn lớp để xác định khoảng ngày hợp lệ.";
}
export async function openHomeroom() {
  if (!state.allowed || !state.catalog) return;
  $("hr-form").reset(); error("hr-form-error");
  const eligible = state.classes.filter(c => c.trangThai === "HOAT_DONG" && state.catalog.namHoc.some(y => y.maNamHoc === c.maNamHoc && y.ketThuc >= state.catalog.homNay));
  options("hr-input-class", eligible, c => c.maLop, c => `${c.tenLop} · ${c.namHoc}`, "Chọn lớp đang hoạt động");
  if (eligible.some(c => c.maLop === Number($("hr-class").value))) $("hr-input-class").value = $("hr-class").value;
  updateDates();
  $("hr-teacher").innerHTML = '<option value="">Đang tải giáo viên…</option>';
  $("hr-save").disabled = true;
  modal("hr-modal").show();
  try {
    state.teachers = (await request("/teacher")).filter(t => t.trangThai === "DANG_CONG_TAC");
    options("hr-teacher", state.teachers, t => t.maGV, t => `${t.hoTen} · ${t.maSoGV}`, "Chọn giáo viên");
    if (!eligible.length) throw new Error("Chưa có lớp đang hoạt động trong năm học hiện tại hoặc tương lai.");
    if (!state.teachers.length) throw new Error("Chưa có giáo viên đang công tác. Hãy bổ sung hồ sơ giáo viên trước.");
    $("hr-save").disabled = false;
  } catch (err) { error("hr-form-error", err.message); }
}
async function saveHomeroom(event) {
  event.preventDefault();
  const button = $("hr-save");
  if (button.disabled || !state.allowed || !$("hr-form").reportValidity()) return;
  button.disabled = true; button.textContent = "Đang lưu…"; error("hr-form-error");
  const body = {maLop:Number($("hr-input-class").value), maGiaoVien:Number($("hr-teacher").value), tuNgay:$("hr-start").value, denNgay:$("hr-end").value || null};
  try {
    await request(endpoint, {method:"POST", body:JSON.stringify(body)});
    modal("hr-modal").hide();
    $("hr-success").textContent = "Đã phân công chủ nhiệm và duyệt trực tiếp thành công.";
    $("hr-success").hidden = false;
    const cls = state.classes.find(c => c.maLop === body.maLop);
    $("hr-year").value = cls.maNamHoc;
    filterClasses(); $("hr-class").value = body.maLop;
    await loadHomeroom();
  } catch (err) { error("hr-form-error", err.message); }
  finally { button.disabled = false; button.textContent = "Lưu phân công"; }
}
async function showDetail(id) {
  const seq = ++state.detailSequence;
  $("hr-detail-body").textContent = "Đang tải…";
  modal("hr-detail-modal").show();
  try {
    const row = await request(`${endpoint}/${id}`);
    if (seq !== state.detailSequence) return;
    const fields = [["Mã phân công", row.maPhanCong], ["Lớp",row.tenLop], ["Năm học",row.namHoc], ["Giáo viên",row.tenGiaoVien],
      ["Từ ngày",formatDate(row.tuNgay)], ["Đến ngày",row.denNgay ? formatDate(row.denNgay) : "Chưa xác định"],
      ["Hiệu lực",effectiveStatus(row)], ["Trạng thái duyệt",row.trangThaiDuyet === "DA_DUYET" ? "Đã duyệt" : row.trangThaiDuyet],
      ["Người duyệt",row.tenNguoiDuyet], ["Thời điểm duyệt",row.duyetLuc?.replace("T"," ")]];
    $("hr-detail-body").innerHTML = '<dl class="detail-grid">' + fields.map(([label,value]) => `<div><dt>${escapeHtml(label)}</dt><dd>${escapeHtml(value ?? "—")}</dd></div>`).join("") + '</dl>';
  } catch (err) { if (seq === state.detailSequence) $("hr-detail-body").textContent = err.message; }
}
