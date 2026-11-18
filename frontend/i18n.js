/* ====== 行程資訊：日期確定後改這裡即可 ====== */
const TRIP = {
  date:     { zh: "出發日期：待定", en: "Departure: TBC" },
  deadline: { zh: "報名截止：待定", en: "Deadline: TBC" },
  returnDate: null   // 例如 "2027-03-10"，用來檢查護照效期是否滿 6 個月
};

/* ====== 文字（中 / 英） ====== */
const T = {
  badge:{zh:"團體旅遊報名",en:"Group Tour Registration"},
  title:{zh:"澳洲 9 日遊 報名表",en:"Australia 9-Day Tour — Registration"},
  subtitle:{zh:"請依護照資料如實填寫，送出後可用同一台裝置回來修改。",en:"Please fill in exactly as shown on your passport. You can come back on this device to edit after submitting."},
  fact_days:{zh:"9 天 8 夜",en:"9 days / 8 nights"},
  fact_date:TRIP.date, fact_deadline:TRIP.deadline,
  s1:{zh:"旅客基本資料",en:"Traveller details"},
  s1_hint:{zh:"英文姓名請與護照完全相同，機票與簽證都會依此開立。",en:"English name must match your passport exactly — flights and visas are issued with it."},
  nameZh:{zh:"中文姓名",en:"Chinese name (if any)"},
  surname:{zh:"護照英文姓",en:"Surname (as in passport)"},
  given:{zh:"護照英文名",en:"Given name(s) (as in passport)"},
  gender:{zh:"性別",en:"Gender"},
  dob:{zh:"出生日期",en:"Date of birth"},
  nationality:{zh:"國籍",en:"Nationality"},
  s2:{zh:"護照與簽證",en:"Passport & visa"},
  s2_hint:{zh:"入境澳洲需持有效簽證或 ETA，護照效期建議在回程日後仍有 6 個月以上。",en:"A valid visa or ETA is required to enter Australia. Your passport should be valid for at least 6 months after return."},
  passportNo:{zh:"護照號碼",en:"Passport number"},
  passportExp:{zh:"護照到期日",en:"Passport expiry date"},
  exp_warn:{zh:"⚠ 護照效期可能不足 6 個月，建議先換發護照。",en:"⚠ Your passport may have less than 6 months' validity — consider renewing it."},
  visa:{zh:"澳洲簽證 / ETA 狀態",en:"Australian visa / ETA status"},
  s3:{zh:"聯絡方式",en:"Contact"},
  phone:{zh:"手機號碼",en:"Mobile number"},
  email:{zh:"電子郵件",en:"Email"},
  im:{zh:"LINE / WeChat ID",en:"LINE / WeChat ID"},
  address:{zh:"通訊地址",en:"Mailing address"},
  s4:{zh:"緊急聯絡人",en:"Emergency contact"},
  s4_hint:{zh:"請填寫不會同行的親友。",en:"Someone who is not travelling with you."},
  emName:{zh:"姓名",en:"Name"}, emRel:{zh:"關係",en:"Relationship"}, emPhone:{zh:"電話",en:"Phone"},
  s5:{zh:"行程需求",en:"Trip preferences"},
  room:{zh:"房型需求",en:"Room preference"},
  roommate:{zh:"指定同房者姓名（如有）",en:"Preferred roommate (if any)"},
  diet:{zh:"飲食需求",en:"Dietary requirements"},
  health:{zh:"過敏、慢性病或其他健康狀況（如有）",en:"Allergies, medical conditions or other health notes (if any)"},
  insurance:{zh:"旅遊平安保險",en:"Travel insurance"},
  s6:{zh:"同行家人 / 朋友",en:"Travelling companions"},
  s6_hint:{zh:"若幫家人一起報名，請在此新增每一位同行者；自行另外報名者則不用填。",en:"Registering family members too? Add each companion here. Skip anyone who will register on their own."},
  add_comp:{zh:"＋ 新增同行者",en:"+ Add companion"},
  comp:{zh:"同行者",en:"Companion"}, remove:{zh:"移除",en:"Remove"},
  relation:{zh:"與報名人關係",en:"Relationship to you"},
  s7:{zh:"備註與確認",en:"Notes & confirmation"},
  notes:{zh:"其他需求或問題",en:"Other requests or questions"},
  consent:{zh:"我確認以上資料正確，並同意主辦方為辦理機票、住宿、簽證及保險等旅遊事宜使用我的個人資料。",en:"I confirm the information above is correct and agree that the organiser may use my personal data to arrange flights, accommodation, visas and insurance for this trip."},
  submit:{zh:"送出報名",en:"Submit registration"},
  update:{zh:"更新報名資料",en:"Update registration"},
  sending:{zh:"送出中…（伺服器剛醒來時可能要等一下）",en:"Submitting… (may take a moment if the server is waking up)"},
  m_req:{zh:"此欄為必填",en:"Required"},
  m_pick:{zh:"請選擇一項",en:"Please choose one"},
  m_email:{zh:"請輸入有效的電子郵件",en:"Please enter a valid email"},
  m_consent:{zh:"請勾選同意後再送出",en:"Please tick to confirm"},
  m_fix:{zh:"還有欄位需要填寫，請檢查紅框處。",en:"Some fields need attention — see the red boxes."},
  m_fail:{zh:"送出失敗，請檢查網路後再試一次。",en:"Could not submit. Please check your connection and try again."},
  m_invalid:{zh:"資料格式有誤，請檢查後再送出。",en:"Some data is invalid — please check and try again."},
  m_gone:{zh:"找不到您先前的報名紀錄，請再按一次送出，將建立新的報名。",en:"Your previous registration couldn't be found. Submit again to create a new one."},
  done_title:{zh:"報名成功！",en:"You're registered!"},
  done_body:{zh:"我們已收到您的資料，將再以電話或 Email 與您聯絡。之後用這台裝置的同一個瀏覽器打開本頁，就能查看或修改。",en:"We've received your details and will be in touch. Open this page again in the same browser on this device to view or edit."},
  ref_no:{zh:"報名編號：",en:"Reference no.: "},
  done_edit:{zh:"查看 / 修改我的報名",en:"View / edit my registration"},
  s_waking:{zh:"⏳ 報名系統喚醒中，約需 30–60 秒，您可以先開始填寫。",en:"⏳ Waking up the registration server (30–60 s). You can start filling in the form."},
  s_down:{zh:"目前無法連線到報名系統。您仍可填寫，稍後再按送出；若一直無法送出請聯絡主辦方。",en:"Can't reach the registration server. You can keep filling in and try submitting later; contact the organiser if it persists."},
  loaded:{zh:"已載入您先前送出的資料，修改後按「更新報名資料」即可。",en:"Your previous submission is loaded — edit and press “Update registration”."},
  footer:{zh:"個人資料僅供本次旅遊辦理使用，只有主辦方看得到。",en:"Your data is used only for this trip and is visible only to the organiser."}
};

/* ====== 選項：代碼要跟後端 Options.java 一致 ====== */
const OPTS = {
  gender:[["M",{zh:"男",en:"Male"}],["F",{zh:"女",en:"Female"}]],
  visa:[["have",{zh:"已持有效簽證 / ETA",en:"Already have visa / ETA"}],["need",{zh:"需要代辦",en:"Need help applying"}],["self",{zh:"自行申請",en:"Will apply myself"}],["unsure",{zh:"不確定",en:"Not sure"}]],
  room:[["twin",{zh:"雙人房（與同行者同住）",en:"Twin/double (with companion)"}],["single",{zh:"單人房（需補差價）",en:"Single room (supplement applies)"}],["triple",{zh:"三人房",en:"Triple room"}],["match",{zh:"由團方安排同房",en:"Please match me with a roommate"}]],
  diet:[["none",{zh:"無特殊需求",en:"No restrictions"}],["veg",{zh:"素食",en:"Vegetarian"}],["vegan",{zh:"全素",en:"Vegan"}],["nobeef",{zh:"不吃牛",en:"No beef"}],["halal",{zh:"清真",en:"Halal"}],["other",{zh:"其他（請寫在備註）",en:"Other (see notes)"}]],
  insurance:[["self",{zh:"自行購買",en:"I'll buy my own"}],["need",{zh:"需要代辦",en:"Please arrange for me"}]]
};

function optLabel(group, value, lang) {
  const o = (OPTS[group] || []).find(x => x[0] === value);
  return o ? o[1][lang || "zh"] : (value || "");
}

window.I18N = { TRIP, T, OPTS, optLabel };
