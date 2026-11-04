import { request, escapeHtml, query, formatDate } from "./records-api.js";
import {
  profiles,
  profileFields,
  profilePage,
  homePage,
  profileModals,
} from "./records-templates.js";

// 1. Trạng thái riêng của UC05–UC07 và trang chủ.
const $ = (id) => document.getElementById(id);
const state = {
  catalog: null,
  classes: [],
  user: null,
  rows: {},
  page: {},
  sequence: {},
  editing: null,
  detailStudent: null,
};
const pageSize = 10;
const showModal = (id) => bootstrap.Modal.getOrCreateInstance($(id)).show();
const hideModal = (id) => bootstrap.Modal.getOrCreateInstance($(id)).hide();
const teacherStatus = (value) =>
  value === "DANG_CONG_TAC"
    ? "Đang công tác"
    : value === "NGUNG_CONG_TAC"
      ? "Ngừng công tác"
      : value || "—";

function showError(id, error) {
  $(id).textContent = error.message;
  $(id).classList.remove("d-none");
}
function notify(message) {
  $("toastMessage").textContent = message;
  bootstrap.Toast.getOrCreateInstance($("successToast")).show();
}
async function withButton(button, task) {
  if (button.disabled) return;
  const label = button.innerHTML;
  button.disabled = true;
  button.textContent = "Đang xử lý…";
  try {
    await task();
  } finally {
    button.disabled = false;
    button.innerHTML = label;
  }
}

export function mountRecords() {
  document
    .querySelector(".app-footer")
    .insertAdjacentHTML(
      "beforebegin",
      homePage + Object.keys(profiles).map(profilePage).join(""),
    );
  document.body.insertAdjacentHTML("beforeend", profileModals);
  const navigation = document.querySelector(".side-nav");
  navigation.insertAdjacentHTML(
    "afterbegin",
    `
    <a href="#home" data-view="home"><span data-icon="school"></span>Trang chủ</a>
    <a href="#students" data-view="students"><span data-icon="users"></span>Học sinh & tìm kiếm</a>
    <a href="#teachers" data-view="teachers"><span data-icon="building"></span>Giáo viên</a>
    <a href="#guardians" data-view="guardians"><span data-icon="shield"></span>Người giám hộ</a>`,
  );
  bindEvents();
}

export function configureRecords(user, catalog, classes) {
  state.user = user;
  state.catalog = catalog;
  state.classes = classes;
  $("home-name").textContent = user.hoTen;
  const options = (rows, id, label, placeholder) =>
    `<option value="">${placeholder}</option>` +
    rows
      .map(
        (row) =>
          `<option value="${row[id]}">${escapeHtml(label(row))}</option>`,
      )
      .join("");
  $("student-year").innerHTML = options(
    catalog.namHoc,
    "maNamHoc",
    (row) => row.ten,
    "Tất cả năm học",
  );
  $("student-grade").innerHTML = options(
    catalog.khoi,
    "maKhoi",
    (row) => `Khối ${row.soKhoi}`,
    "Tất cả khối",
  );
  updateClassFilter();
  const allowed = user.vaiTro.some((role) =>
    ["ADMIN", "BGH", "GIAO_VU"].includes(role),
  );
  document
    .querySelectorAll("[data-profile-add]")
    .forEach((button) => (button.disabled = !allowed));
}

function updateClassFilter() {
  const year = $("student-year").value,
    grade = $("student-grade").value;
  $("student-class").innerHTML =
    '<option value="">Tất cả lớp</option>' +
    state.classes
      .filter(
        (row) =>
          (!year || row.maNamHoc == year) && (!grade || row.maKhoi == grade),
      )
      .map(
        (row) =>
          `<option value="${row.maLop}">${escapeHtml(row.tenLop)} · ${escapeHtml(row.namHoc)}</option>`,
      )
      .join("");
}

// 2. Trang chủ: lấy số liệu từ API nghiệp vụ, không tạo lại module SchoolUi.
async function loadHome() {
  $("home-error").classList.add("d-none");
  $("home-stats").innerHTML =
    '<p class="p-3 text-secondary">Đang tải dữ liệu tổng quan…</p>';
  try {
    const [students, teachers, classes, requests] = await Promise.all([
      request("/student"),
      request("/teacher"),
      request("/api/v1/classes?kichThuoc=1"),
      request("/api/v1/enrollments?trangThai=CHO_DUYET&kichThuoc=1"),
    ]);
    const cards = [
      ["Học sinh", students.length, "students", "users"],
      ["Giáo viên", teachers.length, "teachers", "building"],
      ["Lớp học", classes.tongSoPhanTu, "classes", "grid"],
      ["Yêu cầu chờ duyệt", requests.tongSoPhanTu, "requests", "clock"],
    ];
    $("home-stats").innerHTML = cards
      .map(
        ([label, count, view], index) =>
          `<a href="#${view}" class="stat-card home-stat"><span class="home-stat-index">0${index + 1}</span><div><small>${label}</small><strong>${new Intl.NumberFormat("vi-VN").format(count)}</strong><span>Xem danh sách →</span></div></a>`,
      )
      .join("");
  } catch (error) {
    $("home-stats").innerHTML = "";
    showError("home-error", error);
  }
}

// 3. Tìm kiếm và phân trang hồ sơ. UC06 lọc tại backend, phân trang hiển thị tại trình duyệt.
export async function renderRecords(view) {
  if (view === "home") return loadHome();
  if (profiles[view]) return loadProfiles(view);
}

async function loadProfiles(type) {
  const sequence = (state.sequence[type] = (state.sequence[type] || 0) + 1);
  $(`${type}-feedback`).classList.remove("text-danger");
  $(`${type}-feedback`).textContent = "Đang tải danh sách…";
  $(`${type}-rows`).innerHTML = "";
  $(`${type}-pager`).innerHTML = "";
  try {
    const filters = Object.fromEntries(new FormData($(`${type}-filters`)));
    const path =
      type === "students"
        ? "/student/search?" + query(filters)
        : profiles[type].endpoint;
    let rows = await request(path);
    if (sequence !== state.sequence[type]) return;
    if (type !== "students") {
      const keyword = (filters.hoTen || "").trim().toLocaleLowerCase("vi");
      rows = rows.filter((row) =>
        [row.hoTen, row.maSoGV, row.sdt, row.chuyenMon].some((value) =>
          String(value || "")
            .toLocaleLowerCase("vi")
            .includes(keyword),
        ),
      );
      if (filters.trangThai)
        rows = rows.filter((row) => row.trangThai === filters.trangThai);
    }
    state.rows[type] = rows;
    state.page[type] = 0;
    drawRows(type);
    $(`${type}-feedback`).textContent = "";
  } catch (error) {
    $(`${type}-feedback`).textContent = error.message;
    $(`${type}-feedback`).classList.add("text-danger");
  }
}

function drawRows(type) {
  const rows = state.rows[type],
    page = state.page[type] || 0,
    config = profiles[type];
  const current = rows.slice(page * pageSize, (page + 1) * pageSize);
  $(`${type}-rows`).innerHTML = current.length
    ? current
        .map((row) => {
          const id = row[config.id];
          const code =
            type === "students"
              ? row.maSoHS
              : type === "teachers"
                ? row.maSoGV
                : `#${id}`;
          return `<tr>
      <td><span class="grade-chip">${escapeHtml(code)}</span></td>
      <td><strong>${escapeHtml(row.hoTen)}</strong></td>
      <td>${type === "guardians" ? escapeHtml(row.diaChi || "—") : formatDate(row.ngaySinh)}</td>
      <td>${escapeHtml(row.sdt || "—")}${type !== "guardians" ? `<small class="subtle-label d-block">${escapeHtml(row.email || "")}</small>` : ""}</td>
      <td>${type === "teachers" ? `${escapeHtml(row.chuyenMon || "—")}<small class="d-block subtle-label">${escapeHtml(teacherStatus(row.trangThai))}</small>` : type === "students" ? (row.gioiTinh === "NAM" ? "Nam" : "Nữ") : escapeHtml(row.email || "—")}</td>
      <td><div class="row-actions">
        <button class="btn btn-outline-primary" data-profile-action="detail" data-type="${type}" data-id="${id}">Chi tiết</button>
        <button class="btn btn-light" data-profile-action="edit" data-type="${type}" data-id="${id}" aria-label="Sửa ${escapeHtml(row.hoTen)}">Sửa</button>
        ${type === "teachers" && row.trangThai !== "NGUNG_CONG_TAC" ? `<button class="btn btn-light" data-profile-action="stop" data-type="${type}" data-id="${id}">Ngừng công tác</button>` : ""}
        <button class="btn btn-light text-danger" data-profile-action="delete" data-type="${type}" data-id="${id}" aria-label="Xóa ${escapeHtml(row.hoTen)}">Xóa</button>
      </div></td></tr>`;
        })
        .join("")
    : '<tr><td colspan="6"><div class="empty-state"><strong>Chưa có hồ sơ phù hợp</strong><p>Thử thay đổi bộ lọc hoặc thêm hồ sơ mới.</p></div></td></tr>';
  const totalPages = Math.ceil(rows.length / pageSize);
  $(`${type}-pager`).innerHTML =
    `<span>${rows.length ? `${page * pageSize + 1}–${Math.min((page + 1) * pageSize, rows.length)}` : "0"} / ${rows.length} hồ sơ</span><div class="d-flex gap-2 align-items-center"><button class="btn btn-light" data-profile-page="${type}" data-page="${page - 1}" ${page === 0 ? "disabled" : ""} aria-label="Trang trước">‹</button><span>Trang ${page + 1} / ${Math.max(totalPages, 1)}</span><button class="btn btn-light" data-profile-page="${type}" data-page="${page + 1}" ${page + 1 >= totalPages ? "disabled" : ""} aria-label="Trang sau">›</button></div>`;
}

// 4. Thêm / sửa: trình duyệt validate trước, backend kiểm tra ràng buộc cuối cùng.
function editProfile(type, row = null) {
  state.editing = { type, row };
  $("profile-form").reset();
  $("profile-error").classList.add("d-none");
  $("profile-title").textContent =
    `${row ? "Chỉnh sửa" : "Thêm"} ${profiles[type].singular}`;
  $("profile-fields").innerHTML = profileFields(type);
  if (row)
    profiles[type].fields.forEach(([name]) => {
      $(`profile-${name}`).value = row[name] || "";
    });
  showModal("profileModal");
}

async function saveProfile(event) {
  event.preventDefault();
  await withButton($("profile-save"), async () => {
    $("profile-error").classList.add("d-none");
    try {
      const { type, row } = state.editing,
        config = profiles[type];
      const body = Object.fromEntries(new FormData($("profile-form")));
      Object.keys(body).forEach((key) => {
        body[key] = body[key].trim();
        if (!body[key]) body[key] = null;
      });
      await request(config.endpoint + (row ? `/${row[config.id]}` : ""), {
        method: row ? "PUT" : "POST",
        body: JSON.stringify(body),
      });
      hideModal("profileModal");
      notify("Đã lưu hồ sơ.");
      await loadProfiles(type);
    } catch (error) {
      showError("profile-error", error);
    }
  });
}

function confirm(message, task) {
  $("profile-confirm-message").textContent = message;
  $("profile-confirm-error").classList.add("d-none");
  $("profile-confirm-button").onclick = () =>
    withButton($("profile-confirm-button"), async () => {
      try {
        await task();
        hideModal("profileConfirmModal");
        notify("Đã cập nhật dữ liệu.");
      } catch (error) {
        showError("profile-confirm-error", error);
      }
    });
  showModal("profileConfirmModal");
}

// 5. Chi tiết và người giám hộ của học sinh: xem / liên kết / cập nhật quan hệ / gỡ.
async function detail(type, row, reopen = true) {
  $("profile-detail-title").textContent =
    `${profiles[type].title} · ${row.hoTen}`;
  $("profile-detail-body").innerHTML = "<p>Đang tải hồ sơ…</p>";
  if (reopen) showModal("profileDetailModal");
  const details = profiles[type].fields
    .map(([name, label, fieldType, , options]) => {
      let value = row[name];
      if (fieldType === "select")
        value = options.find(([key]) => key === value)?.[1] || value;
      if (fieldType === "date") value = formatDate(value);
      return `<div><dt>${label}</dt><dd>${escapeHtml(value || "—")}</dd></div>`;
    })
    .join("");
  $("profile-detail-body").innerHTML =
    `<dl class="detail-grid">${details}</dl>`;
  if (type !== "students") return;
  state.detailStudent = row;
  try {
    const [linked, guardians] = await Promise.all([
      request(`/guardian/student/${row.maHS}`),
      request("/guardian"),
    ]);
    $("profile-detail-body").insertAdjacentHTML(
      "beforeend",
      `
      <div class="guardian-heading"><h3>Người giám hộ</h3><a href="#history" id="profile-history-link" class="btn btn-outline-primary">Xem lịch sử xếp lớp</a></div>
      <div id="guardian-link-error" class="alert alert-danger d-none" role="alert"></div>
      <div class="guardian-list">${linked.length ? linked.map((guardian) => `<article><div><strong>${escapeHtml(guardian.hoTen)}</strong><p>${escapeHtml(guardian.moiQuanHe)} · ${escapeHtml(guardian.sdt)} ${guardian.lienHeChinh ? "· Liên hệ chính" : ""}</p></div><button class="btn btn-light text-danger" data-guardian-unlink="${guardian.maNGH}">Gỡ liên kết</button></article>`).join("") : '<p class="text-secondary">Chưa có người giám hộ được liên kết.</p>'}</div>
      <form id="guardian-link-form" class="guardian-link-form"><h4>Thêm / cập nhật liên kết</h4><p class="small text-secondary">Chọn người đã có để cập nhật quan hệ. Tạo hồ sơ mới tại menu Người giám hộ.</p><div class="row g-3">
        <div class="col-md-6"><label for="guardian-link-select" class="form-label">Người giám hộ *</label><select class="form-select" id="guardian-link-select" name="maNGH" required><option value="">Chọn người giám hộ</option>${guardians.map((guardian) => `<option value="${guardian.maNGH}">${escapeHtml(guardian.hoTen)} · ${escapeHtml(guardian.sdt)}</option>`).join("")}</select></div>
        <div class="col-md-6"><label for="guardian-relationship" class="form-label">Quan hệ với học sinh *</label><input class="form-control" id="guardian-relationship" name="moiQuanHe" placeholder="Cha, mẹ, ông, bà…" maxlength="50" required></div>
        <div class="col-12"><label><input type="checkbox" name="lienHeChinh" class="form-check-input me-2">Người liên hệ chính</label></div>
      </div><button class="btn btn-primary mt-3" type="submit">Lưu liên kết</button></form>`,
    );
    $("profile-history-link").onclick = () => {
      hideModal("profileDetailModal");
      sessionStorage.setItem(
        "school.historyStudent",
        JSON.stringify({
          maHocSinh: row.maHS,
          maSoHocSinh: row.maSoHS,
          hoTen: row.hoTen,
        }),
      );
    };
    $("guardian-link-select").onchange = (event) => {
      const selected = linked.find((item) => item.maNGH == event.target.value);
      $("guardian-relationship").value = selected?.moiQuanHe || "";
      $("guardian-link-form").elements.lienHeChinh.checked =
        selected?.lienHeChinh || false;
    };
    $("guardian-link-form").onsubmit = async (event) => {
      event.preventDefault();
      await withButton(event.submitter, async () => {
        try {
          const form = event.target;
          await request("/guardian/link", {
            method: "POST",
            body: JSON.stringify({
              maHS: row.maHS,
              maNGH: Number(form.elements.maNGH.value),
              moiQuanHe: form.elements.moiQuanHe.value.trim(),
              lienHeChinh: form.elements.lienHeChinh.checked,
            }),
          });
          await detail(type, row, false);
          notify("Đã lưu liên kết người giám hộ.");
        } catch (error) {
          showError("guardian-link-error", error);
        }
      });
    };
  } catch (error) {
    $("profile-detail-body").insertAdjacentHTML(
      "beforeend",
      `<p class="text-danger">${escapeHtml(error.message)}</p>`,
    );
  }
}

// 6. Sự kiện: dùng delegation cho các dòng bảng được render lại.
function bindEvents() {
  $("profile-form").addEventListener("submit", saveProfile);
  $("profileModal").addEventListener("shown.bs.modal", () =>
    $("profile-hoTen").focus(),
  );
  document.querySelectorAll("[data-profile-filters]").forEach((form) => {
    form.addEventListener("submit", (event) => {
      event.preventDefault();
      loadProfiles(form.dataset.profileFilters);
    });
    form.addEventListener("reset", () =>
      setTimeout(() => {
        if (form.dataset.profileFilters === "students") updateClassFilter();
        loadProfiles(form.dataset.profileFilters);
      }, 0),
    );
  });
  ["student-year", "student-grade"].forEach((id) =>
    $(id).addEventListener("change", updateClassFilter),
  );
  document.addEventListener("click", async (event) => {
    const add = event.target.closest("[data-profile-add]");
    if (add) return editProfile(add.dataset.profileAdd);
    const page = event.target.closest("[data-profile-page]");
    if (page) {
      state.page[page.dataset.profilePage] = Number(page.dataset.page);
      return drawRows(page.dataset.profilePage);
    }
    const unlink = event.target.closest("[data-guardian-unlink]");
    if (unlink) {
      unlink.disabled = true;
      try {
        await request(
          "/guardian/unlink?" +
            query({
              maHS: state.detailStudent.maHS,
              maNGH: unlink.dataset.guardianUnlink,
            }),
          { method: "DELETE" },
        );
        await detail("students", state.detailStudent, false);
        notify("Đã gỡ liên kết.");
      } catch (error) {
        unlink.disabled = false;
        showError("guardian-link-error", error);
      }
      return;
    }
    const button = event.target.closest("[data-profile-action]");
    if (!button) return;
    const { type, id, profileAction: action } = button.dataset,
      config = profiles[type];
    const row = state.rows[type].find((item) => item[config.id] == id);
    if (action === "edit") return editProfile(type, row);
    if (action === "detail") return detail(type, row);
    if (action === "delete")
      return confirm(
        `Xóa hồ sơ ${row.hoTen}? Chỉ xóa khi không có dữ liệu nghiệp vụ liên quan.${type === "guardians" ? " Các liên kết người giám hộ với học sinh cũng được gỡ." : ""}`,
        async () => {
          await request(config.endpoint + "?" + query({ [config.id]: id }), {
            method: "DELETE",
          });
          await loadProfiles(type);
        },
      );
    if (action === "stop")
      return confirm(
        `Chuyển giáo viên ${row.hoTen} sang trạng thái ngừng công tác? Hồ sơ vẫn được giữ lại.`,
        async () => {
          await request(`/teacher/${id}`, {
            method: "PUT",
            body: JSON.stringify({ ...row, trangThai: "NGUNG_CONG_TAC" }),
          });
          await loadProfiles(type);
        },
      );
  });
}
