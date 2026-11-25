import { login, enterApp, currentUser } from "./auth.js";
const form = document.getElementById("loginForm");
const error = document.getElementById("loginError");
const button = document.getElementById("loginSubmit");

(async () => {
  try {
    const user = await currentUser();
    if (user) enterApp();
  } catch {}
})();
document.getElementById("togglePassword").addEventListener("click", (event) => {
  const input = document.getElementById("password");
  const shown = input.type === "password";
  input.type = shown ? "text" : "password";
  event.currentTarget.textContent = shown ? "Ẩn" : "Hiện";
  event.currentTarget.setAttribute("aria-pressed", String(shown));
  event.currentTarget.setAttribute(
    "aria-label",
    shown ? "Ẩn mật khẩu" : "Hiện mật khẩu",
  );
});
form.addEventListener("submit", async (event) => {
  event.preventDefault();
  if (button.disabled) return;
  error.classList.add("d-none");
  button.disabled = true;
  button.innerHTML =
    '<span class="spinner-border" aria-hidden="true"></span> Đang đăng nhập…';
  try {
    await login(
      document.getElementById("username").value.trim(),
      document.getElementById("password").value,
    );
    enterApp();
  } catch (e) {
    error.textContent =
      e instanceof TypeError
        ? "Không kết nối được server. Kiểm tra ứng dụng đã chạy."
        : e.message;
    error.classList.remove("d-none");
    button.disabled = false;
    button.textContent = "Đăng nhập";
  }
});
