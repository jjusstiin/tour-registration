/* 跟後端溝通的共用工具 */
window.API = (function () {
  const BASE = (window.APP_CONFIG.API_BASE || "").replace(/\/+$/, "");

  async function request(path, { method = "GET", body, editToken, adminToken, timeout = 70000 } = {}) {
    const ctrl = new AbortController();
    const timer = setTimeout(() => ctrl.abort(), timeout);
    const headers = {};
    if (body !== undefined) headers["Content-Type"] = "application/json";
    if (editToken) headers["X-Edit-Token"] = editToken;
    if (adminToken) headers["X-Admin-Token"] = adminToken;
    try {
      const res = await fetch(BASE + path, {
        method, headers, signal: ctrl.signal,
        body: body !== undefined ? JSON.stringify(body) : undefined
      });
      const data = await res.json().catch(() => null);
      if (!res.ok) {
        const err = new Error((data && data.message) || res.statusText);
        err.status = res.status;
        err.data = data;
        throw err;
      }
      return data;
    } catch (e) {
      if (e.status === undefined) e.status = 0; // 0 = 連不上（網路、逾時、CORS）
      throw e;
    } finally {
      clearTimeout(timer);
    }
  }

  /**
   * Render 免費主機閒置 15 分鐘會休眠，第一個請求要等 30–60 秒才醒。
   * 頁面一打開就先叫醒它；超過 2.5 秒沒回應就讓畫面顯示「喚醒中」。
   */
  async function wake(onState) {
    onState("checking");
    const slow = setTimeout(() => onState("waking"), 2500);
    const deadline = Date.now() + 120000;
    try {
      while (Date.now() < deadline) {
        try {
          await request("/api/health", { timeout: 30000 });
          onState("ok");
          return true;
        } catch (e) {
          await new Promise(r => setTimeout(r, 3000));
        }
      }
      onState("down");
      return false;
    } finally {
      clearTimeout(slow);
    }
  }

  return { request, wake };
})();
