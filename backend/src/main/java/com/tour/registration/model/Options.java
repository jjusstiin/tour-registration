package com.tour.registration.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 選項代碼 ↔ 試算表上顯示的中文。前端送代碼（例如 "brisbane"），寫進試算表時轉成中文。
 * 要新增選項時：這裡的對照表與 *_RE、前端 options.js 的 OPTS 都要改。
 */
public final class Options {
    private Options() {
    }

    public static final String DATE_RE = "\\d{4}-\\d{2}-\\d{2}";
    public static final String OPT_DATE_RE = "(\\d{4}-\\d{2}-\\d{2})?";
    public static final String DAY_RE = "(1[4-9]|2[0-2])?";
    public static final String GENDER_RE = "M|F";
    public static final String REGION_RE = "taichung|brisbane|sa|wa|ca|nt|other";
    public static final String ROLE_RE = "youth|staff|escort|other";
    public static final String ATTENDANCE_RE = "full|partial|local";
    public static final String SPORTS_RE = "volleyball|othersport|help|none";
    public static final String MARKET_RE = "join|help|none";
    public static final String MARKET_HELP_RE = "culture|food|reception|setup|other";
    public static final String OTHER_HELP_RE = "planning|photo|translate|logistics|checkin|support|other";
    public static final String LODGING_RE = "need|self";

    public static final Map<String, String> GENDER = map("M", "乾", "F", "坤");
    public static final Map<String, String> REGION = map(
            "taichung", "臺灣臺中", "brisbane", "布里斯本", "sa", "南澳", "wa", "西澳",
            "ca", "中澳", "nt", "北領地", "other", "其他");
    public static final Map<String, String> ROLE = map(
            "youth", "青年參與者(18-30歲)", "staff", "籌備/服務人員", "escort", "隨行輔導人員", "other", "其他");
    public static final Map<String, String> ATTENDANCE = map(
            "full", "全程參與(07/14-07/22)", "partial", "部分日期", "local", "在地參與主要活動(07/17-07/18)");
    public static final Map<String, String> SPORTS = map(
            "volleyball", "參與沙灘排球", "othersport", "參與其他運動賽事聯誼", "help", "願意協助工作", "none", "暫不參與");
    public static final Map<String, String> MARKET = map(
            "join", "願意參與", "help", "願意協助攤位/服務", "none", "暫不參與");
    public static final Map<String, String> MARKET_HELP = map(
            "culture", "文化分享", "food", "美食體驗", "reception", "活動接待", "setup", "場地布置", "other", "其他");
    public static final Map<String, String> OTHER_HELP = map(
            "planning", "活動企劃", "photo", "攝影紀錄", "translate", "翻譯/雙語指引", "logistics", "器材與後勤",
            "checkin", "報到接待", "support", "機動支援", "other", "其他");
    public static final Map<String, String> LODGING = map("need", "需要安排", "self", "自行安排");

    public static String label(Map<String, String> options, String code) {
        return options.getOrDefault(code, code == null ? "" : code);
    }

    /** 單選，選「其他」時接上自填內容：「其他：雪梨」 */
    public static String label(Map<String, String> options, String code, String otherText) {
        String s = label(options, code);
        return "other".equals(code) && otherText != null && !otherText.isBlank() ? s + "：" + otherText : s;
    }

    /** 複選，用「、」串起來，「其他」接上自填內容 */
    public static String labels(Map<String, String> options, List<String> codes, String otherText) {
        return codes.stream().map(c -> label(options, c, otherText)).collect(Collectors.joining("、"));
    }

    private static Map<String, String> map(String... kv) {
        Map<String, String> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            m.put(kv[i], kv[i + 1]);
        }
        return Collections.unmodifiableMap(m);
    }
}
