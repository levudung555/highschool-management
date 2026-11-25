// Access token chỉ lưu trong sessionStorage của tab; không dùng cookie đăng nhập.
const TOKEN = "school.accessToken";
export function clearSession() {
  sessionStorage.removeItem(TOKEN);
}
export function accessToken() {
  return sessionStorage.getItem(TOKEN);
}

function parseJwt(token) {
  try {
    const base64Url = token.split(".")[1];
    const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split("")
        .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
        .join(""),
    );
    return JSON.parse(jsonPayload);
  } catch {
    return null;
  }
}

export async function authorizedFetch(url, options = {}) {
  const token = accessToken();
  const response = await fetch(url, {
    ...options,
    cache: "no-store",
    headers: {
      ...options.headers,
      ...(token ? { Authorization: "Bearer " + token } : {}),
    },
  });
  if (response.status === 401) {
    clearSession();
    if (!location.pathname.endsWith("/login.html")) {
      sessionStorage.setItem("school.returnHash", location.hash);
      location.replace("/login.html");
    }
  }
  return response;
}
export async function currentUser() {
  const token = accessToken();
  if (!token) {
    if (!location.pathname.endsWith("/login.html")) {
      sessionStorage.setItem("school.returnHash", location.hash);
      location.replace("/login.html");
    }
    return null;
  }

  const payload = parseJwt(token);
  if (!payload || !payload.exp || payload.exp * 1000 <= Date.now()) {
    clearSession();
    if (!location.pathname.endsWith("/login.html")) {
      sessionStorage.setItem("school.returnHash", location.hash);
      location.replace("/login.html");
    }
    return null;
  }

  // Lấy vai trò từ claim scope (vd: "ROLE_ADMIN ROLE_GIAO_VIEN" hoặc "ADMIN")
  const scopes = (payload.scope || "")
    .split(" ")
    .filter(Boolean)
    .map((s) => s.replace(/^ROLE_/, ""));

  return {
    sub: payload.sub,
    hoTen: payload.sub || "Người dùng",
    vaiTro: scopes,
  };
}
export async function login(username, password) {
  const response = await fetch("/auth/token", {
    method: "POST",
    cache: "no-store",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });
  const data = await response.json().catch(() => ({}));
  if (!response.ok || (data.code && data.code !== 1000)) {
    throw new Error(
      data.message || "Đăng nhập chưa thành công. Vui lòng thử lại.",
    );
  }
  const token = data.result?.token;
  if (!token) {
    throw new Error("Không nhận được token từ hệ thống.");
  }
  sessionStorage.setItem(TOKEN, token);
  return data.result;
}
export function enterApp() {
  const hash = sessionStorage.getItem("school.returnHash");
  sessionStorage.removeItem("school.returnHash");
  location.replace(
    "/" +
      ([
        "#home",
        "#students",
        "#teachers",
        "#guardians",
        "#classes",
        "#roster",
        "#requests",
        "#history",
      ].includes(hash)
        ? hash
        : "#home"),
  );
}
export async function logout() {
  const token = accessToken();
  try {
    if (token) {
      await fetch("/auth/logout", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ token }),
      });
    }
  } finally {
    clearSession();
    sessionStorage.removeItem("school.returnHash");
    location.replace("/login.html");
  }
}
