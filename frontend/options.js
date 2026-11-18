/* ====== 報名表選項：代碼要跟後端 Options.java 一致 ====== */
(function () {
  const OPTS = {
    gender: [["M", "乾"], ["F", "坤"]],
    region: [["taichung", "臺灣臺中"], ["brisbane", "布里斯本"], ["sa", "南澳"], ["wa", "西澳"], ["ca", "中澳"], ["nt", "北領地"], ["other", "其他"]],
    role: [["youth", "青年參與者(18-30歲)"], ["staff", "籌備／服務人員"], ["escort", "隨行輔導人員"], ["other", "其他"]],
    attendance: [["full", "全程參與（07/14–07/22）"], ["partial", "僅參與部分日期"], ["local", "在地參與主要活動（07/17–07/18）"]],
    sports: [["volleyball", "參與沙灘排球"], ["othersport", "參與其他運動賽事聯誼"], ["help", "願意協助工作"], ["none", "暫不參與"]],
    market: [["join", "願意參與"], ["help", "願意協助攤位／服務"], ["none", "暫不參與"]],
    marketHelp: [["culture", "文化分享"], ["food", "美食體驗"], ["reception", "活動接待"], ["setup", "場地布置"], ["other", "其他"]],
    otherHelp: [["planning", "活動企劃"], ["photo", "攝影紀錄"], ["translate", "翻譯／雙語指引"], ["logistics", "器材與後勤"], ["checkin", "報到接待"], ["support", "機動支援"], ["other", "其他"]],
    lodging: [["need", "需要安排"], ["self", "自行安排"]]
  };

  // 活動日期 07/14 ~ 07/22（部分參與的日期選單）
  const DAYS = ["14", "15", "16", "17", "18", "19", "20", "21", "22"];

  function label(group, code, otherText) {
    const o = (OPTS[group] || []).find(x => x[0] === code);
    const s = o ? o[1] : (code || "");
    return code === "other" && otherText ? `${s}：${otherText}` : s;
  }
  function labels(group, codes, otherText) {
    return (codes || []).map(c => label(group, c, otherText)).join("、");
  }
  function attendanceText(r) {
    return r.attendance === "partial" ? `部分日期：07/${r.partialFrom}–07/${r.partialTo}` : label("attendance", r.attendance);
  }

  window.APP_OPTIONS = { OPTS, DAYS, label, labels, attendanceText };
})();
