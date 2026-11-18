package com.tour.registration.store;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tour.registration.model.Companion;
import com.tour.registration.model.Options;
import com.tour.registration.model.Registration;
import com.tour.registration.model.StoredRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 報名資料 ↔ 試算表一列 的轉換。欄位順序就是試算表的欄位順序。
 * 要加欄位：請加在最後面（「同行者資料」之前也行，但要同時改 toRow / fromRow 的索引）。
 */
public class SheetRowMapper {

    public static final List<String> HEADERS = List.of(
            "報名編號", "修改憑證(雜湊)", "報名時間", "更新時間", "語言",
            "中文姓名", "護照英文姓", "護照英文名", "性別", "出生日期", "國籍",
            "護照號碼", "護照到期日", "簽證/ETA",
            "手機", "Email", "LINE/WeChat", "通訊地址",
            "緊急聯絡人", "緊急聯絡人關係", "緊急聯絡人電話",
            "房型", "指定同房者", "飲食", "健康狀況", "旅遊保險",
            "同行者人數", "本筆總人數", "同行者明細", "其他需求",
            "同行者資料(系統用,請勿修改)");

    private static final TypeReference<List<Companion>> COMPANIONS = new TypeReference<>() {
    };

    private final ObjectMapper json;

    public SheetRowMapper(ObjectMapper json) {
        this.json = json;
    }

    public List<Object> toRow(StoredRegistration s) {
        Registration r = s.data();
        List<Object> row = new ArrayList<>(HEADERS.size());
        row.add(s.id());
        row.add(s.tokenHash());
        row.add(s.submittedAt());
        row.add(s.updatedAt());
        row.add(n(r.lang()));
        row.add(n(r.nameZh()));
        row.add(n(r.surname()));
        row.add(n(r.given()));
        row.add(Options.label(Options.GENDER, r.gender()));
        row.add(n(r.dob()));
        row.add(n(r.nationality()));
        row.add(n(r.passportNo()));
        row.add(n(r.passportExp()));
        row.add(Options.label(Options.VISA, r.visa()));
        row.add(n(r.phone()));
        row.add(n(r.email()));
        row.add(n(r.im()));
        row.add(n(r.address()));
        row.add(n(r.emName()));
        row.add(n(r.emRel()));
        row.add(n(r.emPhone()));
        row.add(Options.label(Options.ROOM, r.room()));
        row.add(n(r.roommate()));
        row.add(Options.label(Options.DIET, r.diet()));
        row.add(n(r.health()));
        row.add(Options.label(Options.INSURANCE, r.insurance()));
        row.add(r.companions().size());
        row.add(r.travellerCount());
        row.add(companionSummary(r.companions()));
        row.add(n(r.notes()));
        row.add(writeJson(r.companions()));
        return row;
    }

    public StoredRegistration fromRow(List<Object> row) {
        Registration r = new Registration(
                get(row, 5), get(row, 6), get(row, 7),
                Options.code(Options.GENDER, get(row, 8)),
                get(row, 9), get(row, 10),
                get(row, 11), get(row, 12),
                Options.code(Options.VISA, get(row, 13)),
                get(row, 14), get(row, 15), get(row, 16), get(row, 17),
                get(row, 18), get(row, 19), get(row, 20),
                Options.code(Options.ROOM, get(row, 21)),
                get(row, 22),
                Options.code(Options.DIET, get(row, 23)),
                get(row, 24),
                Options.code(Options.INSURANCE, get(row, 25)),
                readJson(get(row, 30)),
                get(row, 29),
                true,
                get(row, 4).isEmpty() ? "zh" : get(row, 4));
        return new StoredRegistration(get(row, 0), get(row, 1), get(row, 2), get(row, 3), r);
    }

    /** 給主辦方看的一行文字，例如「CHEN DA MING(男, 1980-01-01, 護照 A123, 到期 2030-01-01, 配偶)」。 */
    static String companionSummary(List<Companion> companions) {
        return companions.stream()
                .map(c -> String.format("%s %s(%s, %s, 護照 %s, 到期 %s, %s)",
                        n(c.surname()), n(c.given()), Options.label(Options.GENDER, c.gender()),
                        n(c.dob()), n(c.passportNo()), n(c.passportExp()), n(c.relation())))
                .collect(Collectors.joining("\n"));
    }

    private String writeJson(List<Companion> companions) {
        try {
            return json.writeValueAsString(companions);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private List<Companion> readJson(String s) {
        if (s.isBlank()) {
            return List.of();
        }
        try {
            return json.readValue(s, COMPANIONS);
        } catch (JsonProcessingException e) {
            return List.of(); // 被手動改壞時不讓整個系統掛掉
        }
    }

    private static String get(List<Object> row, int i) {
        return i < row.size() && row.get(i) != null ? row.get(i).toString() : "";
    }

    private static String n(String s) {
        return s == null ? "" : s;
    }
}
