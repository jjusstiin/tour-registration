package com.tour.registration.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 2027年臺澳青年推動惠民啟蘊交流活動 報名表（對應簡章附錄四）。
 * 選項欄位存代碼，例如 region = "brisbane"。
 */
public record Registration(
        // 一、基本資料
        @NotBlank @Size(max = 50) String nameZh,
        @NotBlank @Size(max = 100) String nameEn,
        @NotBlank @Pattern(regexp = Options.GENDER_RE) String gender,
        @NotBlank @Pattern(regexp = Options.DATE_RE) String dob,
        @NotBlank @Size(max = 30) String phone,
        @NotBlank @Email @Size(max = 120) String email,
        @Size(max = 60) String lineId,
        @NotBlank @Pattern(regexp = Options.REGION_RE) String region,
        @Size(max = 50) String regionOther,
        @Size(max = 50) String mentor,
        @NotBlank @Pattern(regexp = Options.ROLE_RE) String role,
        @Size(max = 50) String roleOther,

        // 二、參與方式與活動意願
        @NotBlank @Pattern(regexp = Options.ATTENDANCE_RE) String attendance,
        @Pattern(regexp = Options.DAY_RE) String partialFrom,
        @Pattern(regexp = Options.DAY_RE) String partialTo,
        @NotEmpty @Size(max = 4) List<@Pattern(regexp = Options.SPORTS_RE) String> sports,
        @NotEmpty @Size(max = 3) List<@Pattern(regexp = Options.MARKET_RE) String> market,
        @Size(max = 5) List<@Pattern(regexp = Options.MARKET_HELP_RE) String> marketHelp,
        @Size(max = 50) String marketHelpOther,
        @Size(max = 7) List<@Pattern(regexp = Options.OTHER_HELP_RE) String> otherHelp,
        @Size(max = 50) String otherHelpOther,

        // 三、其他與聯絡資訊
        @NotBlank @Pattern(regexp = Options.LODGING_RE) String lodging,
        @Pattern(regexp = Options.OPT_DATE_RE) String arrivalDate,
        @Size(max = 60) String arrivalFlight,
        @Pattern(regexp = Options.OPT_DATE_RE) String departureDate,
        @Size(max = 60) String departureFlight,
        @NotBlank @Size(max = 50) String emName,
        @NotBlank @Size(max = 30) String emRel,
        @NotBlank @Size(max = 30) String emPhone,
        @NotBlank @Size(max = 1000) String needs,

        @AssertTrue boolean consent
) {
    /** 沒選到的附屬欄位一律清空，避免殘留資料（例如取消「其他」後留下的文字）。 */
    public Registration {
        sports = list(sports);
        market = list(market);
        otherHelp = list(otherHelp);
        marketHelp = market.contains("help") ? list(marketHelp) : List.of();
        regionOther = "other".equals(region) ? n(regionOther) : "";
        roleOther = "other".equals(role) ? n(roleOther) : "";
        partialFrom = "partial".equals(attendance) ? n(partialFrom) : "";
        partialTo = "partial".equals(attendance) ? n(partialTo) : "";
        marketHelpOther = marketHelp.contains("other") ? n(marketHelpOther) : "";
        otherHelpOther = otherHelp.contains("other") ? n(otherHelpOther) : "";
    }

    @JsonIgnore
    @AssertTrue(message = "選「其他」地區時請填寫內容")
    public boolean isRegionOtherFilled() {
        return !"other".equals(region) || !regionOther.isBlank();
    }

    @JsonIgnore
    @AssertTrue(message = "選「其他」身分時請填寫內容")
    public boolean isRoleOtherFilled() {
        return !"other".equals(role) || !roleOther.isBlank();
    }

    @JsonIgnore
    @AssertTrue(message = "部分日期參與需填寫起訖日，且結束不可早於開始")
    public boolean isPartialRangeValid() {
        if (!"partial".equals(attendance)) {
            return true;
        }
        if (!partialFrom.matches("\\d+") || !partialTo.matches("\\d+")) {
            return false;
        }
        return Integer.parseInt(partialFrom) <= Integer.parseInt(partialTo);
    }

    @JsonIgnore
    @AssertTrue(message = "「暫不參與」不能和其他選項同時勾選")
    public boolean isNoneExclusive() {
        return (!sports.contains("none") || sports.size() == 1)
                && (!market.contains("none") || market.size() == 1);
    }

    private static List<String> list(List<String> l) {
        return l == null ? List.of() : List.copyOf(l);
    }

    private static String n(String s) {
        return s == null ? "" : s;
    }
}
