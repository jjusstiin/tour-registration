package com.tour.registration.store;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tour.registration.model.Options;
import com.tour.registration.model.Registration;
import com.tour.registration.model.StoredRegistration;

import java.util.ArrayList;
import java.util.List;

/**
 * 報名資料 ↔ 試算表一列。
 * 中間的欄位是給主辦方看的中文；最後一欄是完整資料（JSON），系統讀回時以它為準。
 * 所以主辦方在試算表手動改內容只影響試算表本身，不會改到報名者讀回來的資料。
 */
public class SheetRowMapper {

    public static final List<String> HEADERS = List.of(
            "報名編號", "修改憑證(雜湊)", "報名時間", "更新時間",
            "中文姓名", "英文姓名(護照拼音)", "性別", "出生日期",
            "電話", "Email", "LINE ID/其他", "所屬地區", "關心人/壇主", "參與身分",
            "參與日期", "07/17運動聯誼", "07/18文化美食市集", "市集可協助項目", "其他協助意願",
            "住宿安排", "抵達布里斯本日期", "抵達班機時間/號碼", "離開布里斯本日期", "離開班機時間/號碼",
            "緊急聯絡人", "緊急聯絡人關係", "緊急聯絡電話", "飲食限制/過敏原/所需協助",
            "完整資料(系統用,請勿修改)");

    private static final int ID = 0, TOKEN = 1, SUBMITTED = 2, UPDATED = 3;
    private static final int DATA = HEADERS.size() - 1;

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
        row.add(n(r.nameZh()));
        row.add(n(r.nameEn()));
        row.add(Options.label(Options.GENDER, r.gender()));
        row.add(n(r.dob()));
        row.add(n(r.phone()));
        row.add(n(r.email()));
        row.add(n(r.lineId()));
        row.add(Options.label(Options.REGION, r.region(), r.regionOther()));
        row.add(n(r.mentor()));
        row.add(Options.label(Options.ROLE, r.role(), r.roleOther()));
        row.add(attendanceText(r));
        row.add(Options.labels(Options.SPORTS, r.sports(), null));
        row.add(Options.labels(Options.MARKET, r.market(), null));
        row.add(Options.labels(Options.MARKET_HELP, r.marketHelp(), r.marketHelpOther()));
        row.add(Options.labels(Options.OTHER_HELP, r.otherHelp(), r.otherHelpOther()));
        row.add(Options.label(Options.LODGING, r.lodging()));
        row.add(n(r.arrivalDate()));
        row.add(n(r.arrivalFlight()));
        row.add(n(r.departureDate()));
        row.add(n(r.departureFlight()));
        row.add(n(r.emName()));
        row.add(n(r.emRel()));
        row.add(n(r.emPhone()));
        row.add(n(r.needs()));
        row.add(writeJson(r));
        return row;
    }

    /** @throws IllegalArgumentException 最後一欄的 JSON 遺失或被改壞時 */
    public StoredRegistration fromRow(List<Object> row) {
        String data = get(row, DATA);
        try {
            Registration r = json.readValue(data, Registration.class);
            return new StoredRegistration(get(row, ID), get(row, TOKEN), get(row, SUBMITTED), get(row, UPDATED), r);
        } catch (JsonProcessingException | IllegalArgumentException e) {
            throw new IllegalArgumentException("第 " + get(row, ID) + " 筆的完整資料欄無法讀取", e);
        }
    }

    static String attendanceText(Registration r) {
        if ("partial".equals(r.attendance())) {
            return "部分日期：07/" + r.partialFrom() + "-07/" + r.partialTo();
        }
        return Options.label(Options.ATTENDANCE, r.attendance());
    }

    private String writeJson(Registration r) {
        try {
            return json.writeValueAsString(r);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String get(List<Object> row, int i) {
        return i < row.size() && row.get(i) != null ? row.get(i).toString() : "";
    }

    private static String n(String s) {
        return s == null ? "" : s;
    }
}
