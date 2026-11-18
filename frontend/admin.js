(function () {
  const { createApp, ref } = Vue;
  const { label, labels, attendanceText } = window.APP_OPTIONS;
  const TOKEN_KEY = "twau2027-admin-token"; // 存在 sessionStorage：關掉分頁就要重新輸入

  const ss = {
    get(k) { try { return sessionStorage.getItem(k) || ""; } catch (e) { return ""; } },
    set(k, v) { try { sessionStorage.setItem(k, v); } catch (e) { } },
    del(k) { try { sessionStorage.removeItem(k); } catch (e) { } }
  };

  // c.v(registration, index, row)
  const cols = [
    { h: "#", v: (r, i) => i + 1 },
    { h: "報名編號", v: (r, i, row) => row.id },
    { h: "報名時間", v: (r, i, row) => row.submittedAt },
    { h: "更新時間", v: (r, i, row) => row.updatedAt },
    { h: "中文姓名", v: r => r.nameZh },
    { h: "英文姓名", v: r => r.nameEn },
    { h: "性別", v: r => label("gender", r.gender) },
    { h: "出生日期", v: r => r.dob },
    { h: "電話", v: r => r.phone },
    { h: "Email", v: r => r.email },
    { h: "LINE/其他", v: r => r.lineId },
    { h: "所屬地區", v: r => label("region", r.region, r.regionOther) },
    { h: "關心人/壇主", v: r => r.mentor },
    { h: "參與身分", v: r => label("role", r.role, r.roleOther) },
    { h: "參與日期", v: r => attendanceText(r) },
    { h: "07/17 運動聯誼", v: r => labels("sports", r.sports) },
    { h: "07/18 市集", v: r => labels("market", r.market) },
    { h: "市集可協助", v: r => labels("marketHelp", r.marketHelp, r.marketHelpOther) },
    { h: "其他協助意願", v: r => labels("otherHelp", r.otherHelp, r.otherHelpOther), wrap: true },
    { h: "住宿", v: r => label("lodging", r.lodging) },
    { h: "抵達日期", v: r => r.arrivalDate },
    { h: "抵達班機", v: r => r.arrivalFlight },
    { h: "離開日期", v: r => r.departureDate },
    { h: "離開班機", v: r => r.departureFlight },
    { h: "緊急聯絡人", v: r => r.emName },
    { h: "關係", v: r => r.emRel },
    { h: "緊急電話", v: r => r.emPhone },
    { h: "飲食/過敏/所需協助", v: r => r.needs, wrap: true }
  ];

  const groups = [
    { key: "region", title: "所屬地區" },
    { key: "role", title: "參與身分" },
    { key: "attendance", title: "參與日期" },
    { key: "lodging", title: "住宿安排" },
    { key: "sports", title: "07/17 運動聯誼（複選）" },
    { key: "market", title: "07/18 市集（複選）" },
    { key: "marketHelp", title: "市集可協助（複選）" },
    { key: "otherHelp", title: "其他協助意願（複選）" }
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

      function logout() { ss.del(TOKEN_KEY); token.value = ""; data.value = null; }

      const count = (group, code) => ((data.value.stats.breakdown[group] || {})[code]) || 0;

      async function copyCsv() {
        const esc = v => { v = (v ?? "").toString(); return /[",\n]/.test(v) ? '"' + v.replace(/"/g, '""') + '"' : v; };
        const lines = [cols.map(c => esc(c.h)).join(",")]
          .concat(data.value.rows.map((row, i) => cols.map(c => esc(c.v(row.registration, i, row))).join(",")));
        try { await navigator.clipboard.writeText(lines.join("\n")); note.value = "已複製！"; }
        catch (e) { note.value = "瀏覽器不允許複製，請直接開啟試算表。"; }
        setTimeout(() => { note.value = ""; }, 2500);
      }

      if (token.value) load();

      return {
        token, data, loading, error, note, cols, groups, load, logout, copyCsv, count,
        label: (g, c) => label(g, c), sheetUrl: window.APP_CONFIG.SHEET_URL
      };
    }
  }).mount("#app");
})();
