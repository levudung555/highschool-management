import { request } from "./records-api.js";

// Ghép dữ liệu từ các API hiện có; giới hạn số yêu cầu lịch sử chạy đồng thời.
export function studentHistory(id) {
  return request(`/student/${id}/enrollments`);
}

export async function enrollmentStudents() {
  const students = await request("/student");
  return students.map(student => ({
    maHocSinh: student.maHS,
    maSoHocSinh: student.maSoHS,
    hoTen: student.hoTen,
    ngaySinh: student.ngaySinh,
  }));
}

export async function enrollmentHistories(students) {
  const results = new Map();
  let next = 0;
  async function worker() {
    while (next < students.length) {
      const student = students[next++];
      results.set(student.maHocSinh, await studentHistory(student.maHocSinh));
    }
  }
  await Promise.all(Array.from({ length: Math.min(4, students.length) }, worker));
  return results;
}

export function inSchoolYear(enrollment, year, classes) {
  if (!year) return true;
  return classes.some(c => c.maLop === enrollment.maLop && c.maNamHoc === Number(year));
}

export function matchesKeyword(row, keyword) {
  const term = keyword.trim().toLocaleLowerCase("vi");
  return [row.hoTen, row.maSoHocSinh, row.tenLop]
    .some(value => String(value || "").toLocaleLowerCase("vi").includes(term));
}

export function enrollmentPage(rows, page, size) {
  const pages = Math.ceil(rows.length / size);
  const current = Math.max(0, Math.min(page, Math.max(0, pages - 1)));
  return {
    danhSach: rows.slice(current * size, (current + 1) * size),
    trang: current, kichThuoc: size,
    tongSoPhanTu: rows.length, tongSoTrang: pages,
  };
}
