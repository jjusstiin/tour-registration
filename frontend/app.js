(function () {
  const { createApp, reactive, ref, computed, watch, onMounted, nextTick } = Vue;
  const { OPTS, DAYS } = window.APP_OPTIONS;

  const T = window.T;
  const SAVED_KEY = "twau2027-registration"; // 存 { id, token }，用來回來修改
  const LANG_KEY = "twau2027-lang";

  const blankForm = () => ({
    nameZh: "", nameEn: "", gender: "", dob: "", phone: "", email: "", lineId: "",
    region: "", regionOther: "", mentor: "", role: "", roleOther: "",
    attendance: "", partialFrom: "", partialTo: "",
    sports: [], market: [], marketHelp: [], marketHelpOther: "", otherHelp: [], otherHelpOther: "",
    lodging: "", arrivalDate: "", arrivalFlight: "", departureDate: "", departureFlight: "",
    emName: "", emRel: "", emPhone: "", needs: "", consent: false
  });

  const REQUIRED_TEXT = ["nameZh", "nameEn", "dob", "phone", "email", "emName", "emRel", "emPhone", "needs"];
  const REQUIRED_PICK = ["gender", "region", "role", "attendance", "lodging"];
  const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  // 錯誤訊息存 i18n.js 的 key，畫面上再翻成目前語言
  const M = {
    req: "m_req", pick: "m_pick", multi: "m_multi", email: "m_email", consent: "m_consent",
    other: "m_other", partial: "m_partial", range: "m_range"
  };

  const ls = {
    get(k) { try { return localStorage.getItem(k); } catch (e) { return null; } },
    set(k, v) { try { localStorage.setItem(k, v); } catch (e) { } },
    del(k) { try { localStorage.removeItem(k); } catch (e) { } }
  };
  const readSaved = () => { try { const v = JSON.parse(ls.get(SAVED_KEY)); return v && v.id && v.token ? v : null; } catch (e) { return null; } };

  /** 回傳 { 欄位: 錯誤訊息 }，空物件 = 全部正確 */
  function validate(f) {
    const e = {};
    REQUIRED_TEXT.forEach(k => { if (!f[k].trim()) e[k] = M.req; });
    REQUIRED_PICK.forEach(k => { if (!f[k]) e[k] = M.pick; });
    if (f.email.trim() && !EMAIL_RE.test(f.email.trim())) e.email = M.email;
    if (f.region === "other" && !f.regionOther.trim()) e.region = M.other;
    if (f.role === "other" && !f.roleOther.trim()) e.role = M.other;
    if (f.attendance === "partial") {
      if (!f.partialFrom || !f.partialTo) e.attendance = M.partial;
      else if (+f.partialFrom > +f.partialTo) e.attendance = M.range;
    }
    if (!f.sports.length) e.sports = M.multi;
    if (!f.market.length) e.market = M.multi;
    if (!f.consent) e.consent = M.consent;
    return e;
  }

  const tabFromHash = () => (location.hash === "#register" ? "register" : "brochure");

  createApp({
    setup() {
      const lang = ref(ls.get(LANG_KEY) === "en" ? "en" : "zh");
      const t = k => (T[k] && T[k][lang.value] !== undefined) ? T[k][lang.value] : k;
      function setLang(l) {
        lang.value = l;
        ls.set(LANG_KEY, l);
        document.documentElement.lang = l === "zh" ? "zh-Hant" : "en";
        document.title = t("doc_title");
      }
      setLang(lang.value);

      const tab = ref(tabFromHash());
      function go(t) {
        if (tabFromHash() !== t) history.pushState(null, "", t === "register" ? "#register" : "#brochure");
        tab.value = t;
        window.scrollTo({ top: 0, behavior: "smooth" });
      }
      window.addEventListener("popstate", () => { tab.value = tabFromHash(); });

      const form = reactive(blankForm());
      const tried = ref(false);       // 按過送出後才開始顯示紅框
      const submitting = ref(false);
      const done = ref(false);
      const saved = ref(readSaved());
      const server = ref("checking"); // checking | waking | ok | down
      const loadedPrev = ref(false);

      const errKeys = computed(() => tried.value ? validate(form) : {});
      const errs = computed(() => {
        const out = {};
        Object.keys(errKeys.value).forEach(k => { out[k] = t(errKeys.value[k]); });
        return out;
      });
      const bad = k => ({ bad: !!errKeys.value[k] });
      const status = reactive({ key: "", err: false });

      // 「暫不參與」與其他選項互斥：勾了暫不參與就清掉其他；勾其他就取消暫不參與
      ["sports", "market"].forEach(key => {
        watch(() => form[key].slice(), (now, before) => {
          if (now.includes("none") && now.length > 1) {
            form[key] = before.includes("none") ? now.filter(x => x !== "none") : ["none"];
          }
        });
      });

      function fill(r) {
        const blank = blankForm();
        Object.keys(blank).forEach(k => {
          const v = r[k];
          form[k] = v === undefined || v === null ? blank[k] : (Array.isArray(v) ? v.slice() : v);
        });
      }

      function payload() {
        const p = {};
        Object.keys(form).forEach(k => {
          const v = form[k];
          p[k] = typeof v === "string" ? v.trim() : (Array.isArray(v) ? v.slice() : v);
        });
        p.nameEn = p.nameEn.toUpperCase();
        if (!p.market.includes("help")) p.marketHelp = [];
        return p;
      }

      function forget() { ls.del(SAVED_KEY); saved.value = null; }

      async function submit() {
        tried.value = true;
        status.key = "";
        if (Object.keys(errKeys.value).length) {
          status.key = "m_fix"; status.err = true;
          await nextTick();
          const first = document.querySelector(".f.bad");
          if (first) first.scrollIntoView({ behavior: "smooth", block: "center" });
          return;
        }
        submitting.value = true;
        try {
          const body = payload();
          if (saved.value) {
            await API.request(`/api/registrations/${encodeURIComponent(saved.value.id)}`,
              { method: "PUT", body, editToken: saved.value.token });
          } else {
            const res = await API.request("/api/registrations", { method: "POST", body });
            saved.value = { id: res.id, token: res.editToken };
            ls.set(SAVED_KEY, JSON.stringify(saved.value));
          }
          done.value = true;
          loadedPrev.value = false;
          server.value = "ok";
          window.scrollTo({ top: 0, behavior: "smooth" });
        } catch (e) {
          status.err = true;
          if (e.status === 404 && saved.value) { forget(); status.key = "m_gone"; }
          else if (e.status === 400) status.key = "m_invalid";
          else status.key = "m_fail";
        } finally {
          submitting.value = false;
        }
      }

      onMounted(async () => {
        const ok = await API.wake(s => { server.value = s; });
        if (ok && saved.value) {
          try {
            const r = await API.request(`/api/registrations/${encodeURIComponent(saved.value.id)}`,
              { editToken: saved.value.token });
            fill(r.registration);
            loadedPrev.value = true;
          } catch (e) {
            if (e.status === 404) forget();
          }
        }
      });

      return { lang, t, setLang, tab, go, OPTS, DAYS, form, errs, bad, submit, submitting, done, status, saved, server, loadedPrev };
    }
  }).mount("#app");
})();
