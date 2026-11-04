import { authorizedFetch } from "./auth.js";

// Các API hồ sơ do nhánh student cung cấp, dùng cùng Bearer Token với UC08/UC09.
export async function request(path, options = {}) {
  const response = await authorizedFetch(path, {
    ...options,
    headers: {
      ...(options.body ? { "Content-Type": "application/json" } : {}),
      ...options.headers,
    },
  });
  if (response.status === 204) return null;
  const body = await response.json().catch(() => ({}));
  if (!response.ok)
    throw new Error(
      body.message ||
        `Không xử lý được yêu cầu (${response.status}). Vui lòng thử lại.`,
    );
  if (body === false)
    throw new Error("Chưa có thay đổi được lưu. Hãy tải lại dữ liệu.");
  return body;
}

export const escapeHtml = (value) =>
  String(value ?? "").replace(
    /[&<>"']/g,
    (character) =>
      ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[
        character
      ],
  );

export function query(values) {
  return new URLSearchParams(
    Object.entries(values).filter(([, value]) => value !== "" && value != null),
  );
}

export function formatDate(value) {
  return value
    ? new Intl.DateTimeFormat("vi-VN").format(
        new Date(value.slice(0, 10) + "T12:00:00"),
      )
    : "—";
}
