(function () {
  const { createApp, ref } = Vue;
  const { optLabel } = window.I18N;
  const TOKEN_KEY = "au9-admin-token"; // 存在 sessionStorage：關掉分頁就要重新輸入

  const ss = {
    get(k) { try { return sessionStorage.getItem(k) || ""; } catch (e) { return ""; } },
    set(k, v) { try { sessionStorage.setItem(k, v); } catch (e) { } },
    del(k) { try { sessionStorage.removeItem(k); } catch (e) { } }
  };

  const L = (g, v) => optLabel(g, v, "zh");
  const companionText = r => (r.companions || []).map(c =>
    `${c.surname} ${c.given}${c.nameZh ? "（" + c.nameZh + "）" : ""}, ${L("gender", c.gender)}, ${c.dob}, 護照 ${c.passportNo} 到期 ${c.passportExp}, ${c.relation}`
  ).join("\n");

  const cols = [
    { h: "#", v: (r, i) => i + 1 },
    { h: "報名編號", v: r => r.id },
    { h: "報名時間", v: r => r.submittedAt },
    { h: "更新時間", v: r => r.updatedAt },
    { h: "中文姓名", v: r => r.registration.nameZh },
    { h: "英文姓", v: r => r.registration.surname },
    { h: "英文名", v: r => r.registration.given },
    { h: "性別", v: r => L("gender", r.registration.gender) },
    { h: "出生日期", v: r => r.registration.dob },
    { h: "國籍", v: r => r.registration.nationality },
    { h: "護照號碼", v: r => r.registration.passportNo },
    { h: "護照到期", v: r => r.registration.passportExp },
    { h: "簽證/ETA", v: r => L("visa", r.registration.visa) },
    { h: "手機", v: r => r.registration.phone },
    { h: "Email", v: r => r.registration.email },
    { h: "LINE/WeChat", v: r => r.registration.im },
    { h: "地址", v: r => r.registration.address },
    { h: "緊急聯絡人", v: r => r.registration.emName },
    { h: "關係", v: r => r.registration.emRel },
    { h: "緊急電話", v: r => r.registration.emPhone },
    { h: "房型", v: r => L("room", r.registration.room) },
    { h: "指定同房", v: r => r.registration.roommate },
    { h: "飲食", v: r => L("diet", r.registration.diet) },
    { h: "健康狀況", v: r => r.registration.health, wrap: true },
    { h: "保險", v: r => L("insurance", r.registration.insurance) },
    { h: "人數", v: r => 1 + (r.registration.companions || []).length },
    { h: "同行者", v: r => companionText(r.registration), wrap: true },
    { h: "其他需求", v: r => r.registration.notes, wrap: true }
  ];

  const groups = [
    { key: "room", title: "房型" },
    { key: "diet", title: "飲食" },
    { key: "visa", title: "簽證/ETA" },
    { key: "insurance", title: "保險" }
  ];

  createApp({
    setup() {
      const token = ref(ss.get(TOKEN_KEY));
      const data = ref(null);
      const loading = ref(false);
      const error = ref("");
      const note = ref("");

      async function load() {
        loading.value = true; error.value = "";
        try {
          data.value = await API.request("/api/admin/registrations", { adminToken: token.value, timeout: 90000 });
          ss.set(TOKEN_KEY, token.value);
        } catch (e) {
          data.value = null;
          error.value = e.status === 401 ? "密碼錯誤。"
            : e.status === 0 ? "連不上伺服器，請稍後再試。"
            : (e.message || "讀取失敗");
          if (e.status === 401) ss.del(TOKEN_KEY);
        } finally {
          loading.value = false;
        }
      }

      function logout() {
        ss.del(TOKEN_KEY); token.value = ""; data.value = null;
      }

      async function copyCsv() {
        const esc = v => { v = (v ?? "").toString(); return /[",\n]/.test(v) ? '"' + v.replace(/"/g, '""') + '"' : v; };
        const lines = [cols.map(c => esc(c.h)).join(",")]
          .concat(data.value.rows.map((r, i) => cols.map(c => esc(c.v(r, i))).join(",")));
        try { await navigator.clipboard.writeText(lines.join("\n")); note.value = "已複製！"; }
        catch (e) { note.value = "瀏覽器不允許複製，請直接開啟試算表。"; }
        setTimeout(() => { note.value = ""; }, 2500);
      }

      if (token.value) load();

      return {
        token, data, loading, error, note, cols, groups, load, logout, copyCsv,
        label: L, sheetUrl: window.APP_CONFIG.SHEET_URL
      };
    }
  }).mount("#app");
})();
