(function () {
  const { createApp, reactive, ref, computed, onMounted, nextTick } = Vue;
  const { TRIP, T, OPTS } = window.I18N;

  const LANG_KEY = "au9-lang";
  const SAVED_KEY = "au9-registration"; // 存 { id, token }，用來回來修改

  const blankForm = () => ({
    nameZh: "", surname: "", given: "", gender: "", dob: "", nationality: "",
    passportNo: "", passportExp: "", visa: "",
    phone: "", email: "", im: "", address: "",
    emName: "", emRel: "", emPhone: "",
    room: "", roommate: "", diet: "", health: "", insurance: "",
    notes: "", consent: false
  });
  const blankComp = () => ({ surname: "", given: "", gender: "", dob: "", passportNo: "", passportExp: "", relation: "", nameZh: "" });

  const REQUIRED_TEXT = ["surname", "given", "dob", "nationality", "passportNo", "passportExp", "phone", "email", "emName", "emRel", "emPhone"];
  const REQUIRED_PICK = ["gender", "visa", "room", "diet", "insurance"];
  const COMP_TEXT = ["surname", "given", "dob", "passportNo", "passportExp", "relation"];
  const UPPER = ["surname", "given", "passportNo"];
  const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

  const ls = {
    get(k) { try { return localStorage.getItem(k); } catch (e) { return null; } },
    set(k, v) { try { localStorage.setItem(k, v); } catch (e) { } },
    del(k) { try { localStorage.removeItem(k); } catch (e) { } }
  };
  const readSaved = () => { try { const v = JSON.parse(ls.get(SAVED_KEY)); return v && v.id && v.token ? v : null; } catch (e) { return null; } };

  /** 回傳 { 欄位key: 錯誤訊息key }，空物件 = 全部正確 */
  function validate(form, companions) {
    const e = {};
    REQUIRED_TEXT.forEach(k => { if (!String(form[k] || "").trim()) e[k] = "m_req"; });
    REQUIRED_PICK.forEach(k => { if (!form[k]) e[k] = "m_pick"; });
    if (form.email.trim() && !EMAIL_RE.test(form.email.trim())) e.email = "m_email";
    if (!form.consent) e.consent = "m_consent";
    companions.forEach(c => {
      COMP_TEXT.forEach(k => { if (!String(c[k] || "").trim()) e[`c.${c.uid}.${k}`] = "m_req"; });
      if (!c.gender) e[`c.${c.uid}.gender`] = "m_pick";
    });
    return e;
  }

  function clean(obj, upperKeys) {
    const o = {};
    Object.keys(obj).forEach(k => {
      let v = obj[k];
      if (typeof v === "string") { v = v.trim(); if (upperKeys.includes(k)) v = v.toUpperCase(); }
      o[k] = v;
    });
    return o;
  }

  createApp({
    setup() {
      const lang = ref(ls.get(LANG_KEY) === "en" ? "en" : "zh");
      const t = k => (T[k] && T[k][lang.value]) || k;
      function setLang(l) {
        lang.value = l;
        ls.set(LANG_KEY, l);
        document.documentElement.lang = l === "zh" ? "zh-Hant" : "en";
        document.title = t("title");
      }

      const form = reactive(blankForm());
      const companions = reactive([]);
      const tried = ref(false);       // 按過送出後才開始顯示紅框
      const submitting = ref(false);
      const done = ref(false);
      const status = reactive({ key: "", err: false });
      const saved = ref(readSaved());
      const server = ref("checking"); // checking | waking | ok | down
      const loadedPrev = ref(false);

      const errs = computed(() => tried.value ? validate(form, companions) : {});
      const bad = k => ({ bad: !!errs.value[k] });
      const msg = k => errs.value[k] ? t(errs.value[k]) : "";
      const cbad = (c, k) => bad(`c.${c.uid}.${k}`);
      const cmsg = (c, k) => msg(`c.${c.uid}.${k}`);

      let uidSeq = 0;
      function addComp(data) {
        companions.push(Object.assign(blankComp(), data || {}, { uid: ++uidSeq }));
      }

      function expWarn(v) {
        if (!v) return false;
        const base = TRIP.returnDate ? new Date(TRIP.returnDate) : new Date();
        const limit = new Date(base);
        limit.setMonth(limit.getMonth() + (TRIP.returnDate ? 6 : 9));
        return new Date(v) < limit;
      }

      function fill(r) {
        const blank = blankForm();
        Object.keys(blank).forEach(k => { form[k] = r[k] !== undefined && r[k] !== null ? r[k] : blank[k]; });
        companions.splice(0);
        (r.companions || []).forEach(c => addComp(c));
      }

      function payload() {
        const p = clean(form, UPPER);
        p.companions = companions.map(({ uid, ...c }) => clean(c, UPPER));
        p.lang = lang.value;
        return p;
      }

      function forget() {
        ls.del(SAVED_KEY);
        saved.value = null;
      }

      async function submit() {
        tried.value = true;
        status.key = "";
        if (Object.keys(errs.value).length) {
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
        setLang(lang.value);
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

      return {
        lang, t, setLang, OPTS, form, companions, addComp, expWarn,
        bad, msg, cbad, cmsg, submit, submitting, done, status, saved, server, loadedPrev
      };
    }
  }).mount("#app");
})();
