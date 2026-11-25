import { request, escapeHtml, formatDate } from "./records-api.js";

const panel = document.getElementById("grade10Panel");
const toggle = document.getElementById("toggleGrade10");
const field = (name) => document.getElementById("grade10-" + name);
const classApi = "/api/v1/classes";
const batchApi = "/api/v1/enrollments/batches";

let catalog;
let proposals = [];
let preparedClasses = [];
let existingNames = new Set();
let preview;

// Disable the whole panel while a request runs to prevent double submission.
async function run(action) {
  if (field("workspace").disabled) return;
  field("workspace").disabled = true;
  panel.setAttribute("aria-busy", "true");
  showMessage();
  try {
    await action();
  } catch (error) {
    showMessage(error.message, "danger");
  } finally {
    field("workspace").disabled = false;
    panel.removeAttribute("aria-busy");
  }
}

function showMessage(message = "", kind = "info") {
  field("message").textContent = message;
  field("message").className = message ? "alert alert-" + kind : "";
}

function resetPreview() {
  preview = null;
  field("review").hidden = true;
  field("rosters").replaceChildren();
}

function resetPlan() {
  resetPreview();
  field("planning").hidden = true;
  field("proposals").replaceChildren();
  showMessage();
}

function selectedYear() {
  return catalog.namHoc.find((year) => year.maNamHoc === Number(field("year").value));
}

async function loadYears() {
  if (catalog) return;
  catalog = await request(classApi + "/catalog");
  for (const year of catalog.namHoc) {
    field("year").add(new Option(year.ten, year.maNamHoc));
  }
  const active = catalog.namHoc.find((year) =>
    year.batDau <= catalog.homNay && catalog.homNay <= year.ketThuc);
  const upcoming = [...catalog.namHoc].reverse().find((year) => year.batDau > catalog.homNay);
  if (active || upcoming) field("year").value = (active || upcoming).maNamHoc;
  field("load").disabled = !catalog.namHoc.length;
  if (!catalog.namHoc.length) showMessage("Chưa có năm học để xếp lớp.");
}

async function loadClassNames(maNamHoc) {
  const names = new Set();
  let page = 0;
  let totalPages;
  do {
    const query = new URLSearchParams({ maNamHoc, trang: page, kichThuoc: 100 });
    const result = await request(classApi + "?" + query);
    for (const item of result.danhSach) names.add(item.tenLop.toUpperCase());
    totalPages = result.tongSoTrang;
    page++;
  } while (page < totalPages);
  return names;
}

function showConfirmed(ngayVaoLop, soHocSinh) {
  preview = null;
  field("planning").hidden = true;
  field("review").hidden = false;
  field("confirmForm").hidden = true;
  field("reviewTitle").textContent = "Đợt xếp lớp đã chốt";
  const summary = soHocSinh == null ? "" : soHocSinh + " học sinh đã được xếp. ";
  field("reviewInfo").textContent = summary + "Ngày vào lớp " + formatDate(ngayVaoLop) + ".";
  const link = document.createElement("a");
  link.href = "#roster";
  link.textContent = "Mở danh sách lớp";
  link.addEventListener("click", () => {
    document.getElementById("rosterDate").value = ngayVaoLop;
  });
  field("reviewInfo").append(" ", link);
}

async function loadProposals() {
  resetPlan();
  const year = selectedYear();
  if (!year) return;
  const query = new URLSearchParams({ maNamHoc: year.maNamHoc });
  const status = await request(batchApi + "/status?" + query);
  if (status.ngayVaoLopDaChot) {
    showConfirmed(status.ngayVaoLopDaChot);
    return;
  }
  if (year.ketThuc < catalog.homNay) {
    showMessage("Năm học đã kết thúc.");
    return;
  }
  const capacity = Number(field("capacity").value);
  if (!Number.isInteger(capacity) || capacity < 1 || capacity > 65535) {
    throw new Error("Sĩ số mặc định phải là số nguyên từ 1 đến 65535.");
  }
  query.set("chiTieuLopMoi", capacity);
  [proposals, existingNames] = await Promise.all([
    request(classApi + "/grade-10/opening-proposal?" + query),
    loadClassNames(year.maNamHoc),
  ]);
  preparedClasses = status.lopDaChuanBi;
  if (!proposals.some((proposal) => proposal.tongNguyenVong > 0)) {
    showMessage("Năm học này chưa có hồ sơ nhập học.");
    return;
  }
  renderProposals();
  field("planning").hidden = false;
}

function renderProposals() {
  for (const proposal of proposals) {
    if (!proposal.tongNguyenVong) continue;
    const section = document.createElement("section");
    section.className = "grade10-combination";
    section.dataset.toHop = proposal.maToHop;
    const classes = preparedClasses.filter((lop) => lop.maToHop === proposal.maToHop);
    section.innerHTML =
      '<header class="grade10-group-heading"><h4>Tổ hợp ' + escapeHtml(proposal.kyHieu) +
      '</h4><span>' + proposal.tongNguyenVong + " hồ sơ</span></header>" +
      '<p class="grade10-capacity">Đã chuẩn bị <strong>' + proposal.soLopDaChuanBi + " lớp · " +
      proposal.tongSucChua + " chỗ</strong><br>Đề xuất mở thêm <strong>" + proposal.soLopCanMoThem + " lớp</strong></p>" +
      (classes.length ? '<ul class="mb-2">' + classes.map((lop) =>
        "<li>" + escapeHtml(lop.tenLop) + " · " + lop.siSoToiDa +
        " chỗ · thứ tự " + lop.thuTuXep + "</li>").join("") + "</ul>" : "") +
      '<div class="grade10-drafts"></div>' +
      '<button type="button" class="btn btn-sm btn-outline-primary">Thêm lớp dự kiến</button>' +
      '<p class="small mt-2 mb-0" data-capacity-summary></p>';
    field("proposals").append(section);
    section.querySelector("button").onclick = () => addDraft(section);
    for (let i = 0; i < proposal.soLopCanMoThem; i++) addDraft(section, false);
  }
  updateTotals();
}

function addDraft(section, update = true) {
  // Tên lớp phải duy nhất trong năm. Thứ tự chỉ cần duy nhất trong tổ hợp.
  const names = new Set(existingNames);
  for (const input of panel.querySelectorAll("[data-class-name]")) {
    names.add(input.value.trim().toUpperCase());
  }
  let index = 1;
  while (names.has("10A" + index)) index++;
  const orders = preparedClasses
    .filter((lop) => lop.maToHop === Number(section.dataset.toHop))
    .map((lop) => lop.thuTuXep);
  for (const input of section.querySelectorAll("[data-class-order]")) orders.push(Number(input.value));
  const order = Math.max(0, ...orders) + 1;

  const row = document.createElement("div");
  row.className = "draft row g-2 align-items-end";
  row.innerHTML =
    '<label class="col-sm-4">Tên lớp<input class="form-control" maxlength="30" required data-class-name></label>' +
    '<label class="col-sm-3">Sĩ số tối đa<input type="number" min="1" max="65535" class="form-control" required data-class-capacity></label>' +
    '<label class="col-sm-3">Thứ tự xếp<input type="number" min="1" max="65535" class="form-control" required data-class-order></label>' +
    '<div class="col-sm-2"><button type="button" class="btn btn-outline-secondary">Bỏ lớp</button></div>';
  row.querySelector("[data-class-name]").value = "10A" + index;
  row.querySelector("[data-class-capacity]").value = field("capacity").value;
  row.querySelector("[data-class-order]").value = order;
  row.querySelector("button").onclick = () => {
    row.remove();
    updateTotals();
  };
  section.querySelector(".grade10-drafts").append(row);
  if (update) updateTotals();
}

function updateTotals() {
  resetPreview();
  let missingSeats = false;
  for (const section of field("proposals").querySelectorAll("[data-to-hop]")) {
    const proposal = proposals.find((item) => item.maToHop === Number(section.dataset.toHop));
    let capacity = proposal.tongSucChua;
    for (const input of section.querySelectorAll("[data-class-capacity]")) {
      capacity += Number(input.value);
    }
    const missing = Math.max(0, proposal.tongNguyenVong - capacity);
    missingSeats ||= missing > 0;
    const summary = section.querySelector("[data-capacity-summary]");
    summary.classList.toggle("grade10-shortage", missing > 0);
    summary.textContent =
      capacity + " chỗ sau điều chỉnh" + (missing ? " · Còn thiếu " + missing + " chỗ" : " · Đủ chỗ");
  }
  const hasDrafts = Boolean(panel.querySelector(".draft"));
  field("openClasses").disabled = !hasDrafts;
  field("preview").disabled = hasDrafts || missingSeats;
  field("next").textContent = missingSeats ? "Bổ sung chỗ cho các tổ hợp còn thiếu." :
    hasDrafts ? "Xác nhận tạo lớp để mở danh sách xếp thử." : "";
}

async function openClasses() {
  const danhSachLop = [];
  const names = new Set(existingNames);
  for (const section of field("proposals").querySelectorAll("[data-to-hop]")) {
    const maToHop = Number(section.dataset.toHop);
    const orders = new Set(preparedClasses.filter((lop) => lop.maToHop === maToHop).map((lop) => lop.thuTuXep));
    for (const row of section.querySelectorAll(".draft")) {
      const nameInput = row.querySelector("[data-class-name]");
      const capacityInput = row.querySelector("[data-class-capacity]");
      const orderInput = row.querySelector("[data-class-order]");
      const tenLop = nameInput.value.trim();
      const siSoToiDa = Number(capacityInput.value);
      const thuTuXep = Number(orderInput.value);
      // Fieldset đang bị khóa khi gửi, nên kiểm tra giá trị trực tiếp.
      const validNumber = (value) => Number.isInteger(value) && value >= 1 && value <= 65535;
      if (!tenLop || tenLop.length > 30 || !validNumber(siSoToiDa) || !validNumber(thuTuXep)) {
        throw new Error("Nhập tên lớp, sĩ số và thứ tự hợp lệ cho từng lớp.");
      }
      const nameKey = tenLop.toUpperCase();
      if (names.has(nameKey)) throw new Error("Tên lớp " + tenLop + " đã được sử dụng trong năm học.");
      if (orders.has(thuTuXep)) throw new Error("Thứ tự " + thuTuXep + " đã được dùng trong tổ hợp của " + tenLop + ".");
      names.add(nameKey);
      orders.add(thuTuXep);
      danhSachLop.push({ maToHop, tenLop, siSoToiDa, thuTuXep });
    }
  }
  await request(classApi + "/grade-10/opening-confirmation", {
    method: "POST",
    body: JSON.stringify({ maNamHoc: selectedYear().maNamHoc, danhSachLop }),
  });
  document.dispatchEvent(new Event("grade10-classes-changed"));
  await loadProposals();
  showMessage("Đã xác nhận tạo lớp.", "success");
}

function rosterMarkup(lop) {
  const students = lop.danhSachHocSinh;
  const rows = students.map((student) =>
    "<tr><td>" + escapeHtml(student.maSoHocSinh) + "</td><td>" + escapeHtml(student.hoTen) +
    "</td><td>" + escapeHtml(student.tongDiemDauVao) + "</td></tr>").join("");
  return "<details><summary>" + escapeHtml(lop.tenLop) + " <small>" + students.length + " học sinh</small></summary>" +
    (rows ? '<div class="table-responsive"><table class="table"><thead><tr>' +
      '<th scope="col">Mã học sinh</th><th scope="col">Họ tên</th><th scope="col">Điểm đầu vào</th>' +
      "</tr></thead><tbody>" + rows + "</tbody></table></div>" : "") + "</details>";
}

async function showPreview() {
  resetPreview();
  const year = selectedYear();
  const query = new URLSearchParams({ maNamHoc: year.maNamHoc });
  preview = await request(batchApi + "/preview?" + query);
  field("reviewTitle").textContent = "Danh sách xếp thử";
  field("reviewInfo").textContent = "Kiểm tra danh sách từng lớp rồi chọn ngày vào lớp chung.";
  field("rosters").innerHTML = preview.danhSachToHop.map((group) =>
    "<h4 class='h6 mt-3'>" + escapeHtml(group.kyHieu) + "</h4>" +
    group.danhSachLop.map(rosterMarkup).join("")).join("");

  const date = field("date");
  date.min = year.batDau > catalog.homNay ? year.batDau : catalog.homNay;
  date.max = year.ketThuc;
  date.value = date.min;
  field("confirmForm").hidden = false;
  field("review").hidden = false;
}

async function confirmPlacement() {
  if (!preview) return;
  const ngayVaoLop = field("date").value;
  if (!ngayVaoLop || ngayVaoLop < field("date").min || ngayVaoLop > field("date").max) {
    throw new Error("Chọn ngày vào lớp hợp lệ trong năm học.");
  }
  if (!window.confirm("Chốt toàn bộ danh sách với ngày vào lớp " + formatDate(ngayVaoLop) + "?")) return;

  // Gửi lại mã từ bản xem trước để backend kiểm tra đúng danh sách đã duyệt.
  const result = await request(batchApi + "/confirm", {
    method: "POST",
    body: JSON.stringify({
      maNamHoc: preview.maNamHoc,
      maKiemTraPhuongAn: preview.maKiemTraPhuongAn,
      ngayVaoLop,
    }),
  });
  field("rosters").replaceChildren();
  showConfirmed(result.ngayVaoLop, result.soHocSinhDaXep);
  document.dispatchEvent(new Event("grade10-classes-changed"));
}

toggle.onclick = () => {
  panel.hidden = !panel.hidden;
  toggle.setAttribute("aria-expanded", String(!panel.hidden));
  if (!panel.hidden) run(loadYears);
};
field("year").onchange = resetPlan;
field("capacity").oninput = resetPlan;
field("proposals").oninput = updateTotals;
field("load").onclick = () => run(loadProposals);
field("openClasses").onclick = () => run(openClasses);
field("preview").onclick = () => run(showPreview);
field("confirmForm").onsubmit = (event) => {
  event.preventDefault();
  run(confirmPlacement);
};
