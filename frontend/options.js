/* ====== 報名表選項（中 / 英）：代碼要跟後端 Options.java 一致 ====== */
(function () {
  const o = (zh, en) => ({ zh, en });
  const OPTS = {
    gender: [["M", o("乾", "Male (乾)")], ["F", o("坤", "Female (坤)")]],
    region: [
      ["taichung", o("臺灣臺中", "Taichung, Taiwan")], ["brisbane", o("布里斯本", "Brisbane")],
      ["sa", o("南澳", "South Australia")], ["wa", o("西澳", "Western Australia")],
      ["ca", o("中澳", "Central Australia")], ["nt", o("北領地", "Northern Territory")], ["other", o("其他", "Other")]],
    role: [
      ["youth", o("青年參與者(18-30歲)", "Youth participant (18–30)")], ["staff", o("籌備／服務人員", "Organiser / volunteer")],
      ["escort", o("隨行輔導人員", "Accompanying mentor")], ["other", o("其他", "Other")]],
    attendance: [
      ["full", o("全程參與（07/14–07/22）", "Whole program (14–22 July)")],
      ["partial", o("僅參與部分日期", "Some days only")],
      ["local", o("在地參與主要活動（07/17–07/18）", "Main events locally (17–18 July)")]],
    sports: [
      ["volleyball", o("參與沙灘排球", "Beach volleyball")], ["othersport", o("參與其他運動賽事聯誼", "Other sports games")],
      ["help", o("願意協助工作", "Happy to help out")], ["none", o("暫不參與", "Not joining")]],
    market: [
      ["join", o("願意參與", "Will join")], ["help", o("願意協助攤位／服務", "Happy to help with stalls / service")],
      ["none", o("暫不參與", "Not joining")]],
    marketHelp: [
      ["culture", o("文化分享", "Cultural sharing")], ["food", o("美食體驗", "Food tasting")],
      ["reception", o("活動接待", "Reception")], ["setup", o("場地布置", "Venue setup")], ["other", o("其他", "Other")]],
    otherHelp: [
      ["planning", o("活動企劃", "Event planning")], ["photo", o("攝影紀錄", "Photography")],
      ["translate", o("翻譯／雙語指引", "Translation / bilingual guide")], ["logistics", o("器材與後勤", "Equipment & logistics")],
      ["checkin", o("報到接待", "Check-in & reception")], ["support", o("機動支援", "Floating support")], ["other", o("其他", "Other")]],
    lodging: [["need", o("需要安排", "Please arrange for me")], ["self", o("自行安排", "I'll arrange my own")]]
  };

  // 活動日期 07/14 ~ 07/22（部分參與的日期選單）
  const DAYS = ["14", "15", "16", "17", "18", "19", "20", "21", "22"];

  function label(group, code, otherText, lang) {
    const x = (OPTS[group] || []).find(e => e[0] === code);
    const s = x ? x[1][lang || "zh"] : (code || "");
    return code === "other" && otherText ? `${s}：${otherText}` : s;
  }
  function labels(group, codes, otherText, lang) {
    return (codes || []).map(c => label(group, c, otherText, lang)).join("、");
  }
  function attendanceText(r) {
    return r.attendance === "partial" ? `部分日期：07/${r.partialFrom}–07/${r.partialTo}` : label("attendance", r.attendance);
  }

  window.APP_OPTIONS = { OPTS, DAYS, label, labels, attendanceText };
})();
