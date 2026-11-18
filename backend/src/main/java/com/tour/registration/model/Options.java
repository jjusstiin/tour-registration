package com.tour.registration.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 選項代碼 ↔ 試算表上顯示的中文。
 * 前端送代碼（例如 "twin"），寫進試算表時轉成中文（「雙人房」），讀回來時再轉回代碼。
 * 要新增選項時：這裡、前端 i18n.js 的 OPTS、以及下面的 *_RE 三處都要改。
 */
public final class Options {
    private Options() {
    }

    public static final String DATE_RE = "\\d{4}-\\d{2}-\\d{2}";
    public static final String GENDER_RE = "M|F";
    public static final String VISA_RE = "have|need|self|unsure";
    public static final String ROOM_RE = "twin|single|triple|match";
    public static final String DIET_RE = "none|veg|vegan|nobeef|halal|other";
    public static final String INSURANCE_RE = "self|need";

    public static final Map<String, String> GENDER = map("M", "男", "F", "女");
    public static final Map<String, String> VISA = map(
            "have", "已持有效簽證/ETA", "need", "需要代辦", "self", "自行申請", "unsure", "不確定");
    public static final Map<String, String> ROOM = map(
            "twin", "雙人房", "single", "單人房", "triple", "三人房", "match", "由團方安排同房");
    public static final Map<String, String> DIET = map(
            "none", "無特殊需求", "veg", "素食", "vegan", "全素", "nobeef", "不吃牛",
            "halal", "清真", "other", "其他");
    public static final Map<String, String> INSURANCE = map("self", "自行購買", "need", "需要代辦");

    public static String label(Map<String, String> options, String code) {
        return options.getOrDefault(code, code == null ? "" : code);
    }

    public static String code(Map<String, String> options, String label) {
        for (Map.Entry<String, String> e : options.entrySet()) {
            if (e.getValue().equals(label)) {
                return e.getKey();
            }
        }
        return label; // 主辦方在試算表手動改成其他文字時，原樣保留
    }

    private static Map<String, String> map(String... kv) {
        Map<String, String> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            m.put(kv[i], kv[i + 1]);
        }
        return Map.copyOf(m);
    }
}
