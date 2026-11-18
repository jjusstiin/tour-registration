package com.tour.registration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tour.registration.model.Registration;
import com.tour.registration.model.StoredRegistration;
import com.tour.registration.store.SheetRowMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 用記憶體暫存跑一輪完整流程：報名 → 讀回 → 修改 → 主辦方查看。 */
@SpringBootTest(properties = "app.admin-token=test-admin")
@AutoConfigureMockMvc
class RegistrationApiTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper json;

    static final String VALID = """
            {"nameZh":"陳大明","nameEn":"CHEN TA MING","gender":"M","dob":"2002-01-02",
             "phone":"0912345678","email":"chen@example.com","lineId":"chen01",
             "region":"other","regionOther":"雪梨","mentor":"王點傳師",
             "role":"youth","roleOther":"殘留文字",
             "attendance":"partial","partialFrom":"16","partialTo":"19",
             "sports":["volleyball","help"],"market":["join","help"],
             "marketHelp":["food","other"],"marketHelpOther":"手作",
             "otherHelp":["photo"],"otherHelpOther":"會被清掉",
             "lodging":"need","arrivalDate":"2027-07-15","arrivalFlight":"BR315 06:30",
             "departureDate":"","departureFlight":"",
             "emName":"陳太太","emRel":"母親","emPhone":"0922000000",
             "needs":"=HYPERLINK(\\"x\\") 不吃牛","consent":true}
            """;

    @Test
    void fullFlow() throws Exception {
        String body = mvc.perform(post("/api/registrations").contentType(MediaType.APPLICATION_JSON).content(VALID))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode created = json.readTree(body);
        String id = created.get("id").asText();
        String token = created.get("editToken").asText();
        assertThat(id).startsWith("TA-");

        mvc.perform(get("/api/registrations/" + id).header("X-Edit-Token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registration.nameEn").value("CHEN TA MING"))
                .andExpect(jsonPath("$.registration.roleOther").value(""))        // 沒選「其他」→ 清空
                .andExpect(jsonPath("$.registration.otherHelpOther").value(""))
                .andExpect(jsonPath("$.registration.marketHelp[1]").value("other"))
                .andExpect(jsonPath("$.registration.regionOtherFilled").doesNotExist());

        mvc.perform(get("/api/registrations/" + id).header("X-Edit-Token", "wrong"))
                .andExpect(status().isNotFound());

        mvc.perform(put("/api/registrations/" + id).header("X-Edit-Token", token)
                        .contentType(MediaType.APPLICATION_JSON).content(VALID.replace("\"need\"", "\"self\"")))
                .andExpect(status().isOk());

        mvc.perform(get("/api/admin/registrations"))
                .andExpect(status().isUnauthorized());

        mvc.perform(get("/api/admin/registrations").header("X-Admin-Token", "test-admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stats.registrations").value(1))
                .andExpect(jsonPath("$.stats.breakdown.lodging.self").value(1))
                .andExpect(jsonPath("$.stats.breakdown.sports.volleyball").value(1))
                .andExpect(jsonPath("$.stats.breakdown.sports.help").value(1))
                .andExpect(jsonPath("$.rows[0].registration.lodging").value("self"));
    }

    @Test
    void rejectsInvalid() throws Exception {
        String[][] bad = {
                {"\"nameZh\":\"陳大明\"", "\"nameZh\":\"\""},                       // 必填空白
                {"\"consent\":true", "\"consent\":false"},                         // 未同意
                {"\"lodging\":\"need\"", "\"lodging\":\"hotel\""},                 // 不存在的選項
                {"\"regionOther\":\"雪梨\"", "\"regionOther\":\"\""},              // 選其他但沒填
                {"\"partialTo\":\"19\"", "\"partialTo\":\"15\""},                  // 結束早於開始
                {"\"partialTo\":\"19\"", "\"partialTo\":\"25\""},                  // 超出活動日期
                {"\"sports\":[\"volleyball\",\"help\"]", "\"sports\":[\"none\",\"help\"]"}, // 暫不參與+其他
                {"\"sports\":[\"volleyball\",\"help\"]", "\"sports\":[]"},          // 沒勾
        };
        for (String[] b : bad) {
            assertThat(VALID).contains(b[0]);
            mvc.perform(post("/api/registrations").contentType(MediaType.APPLICATION_JSON)
                            .content(VALID.replace(b[0], b[1])))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void sheetRowRoundTrip() throws Exception {
        Registration reg = json.readValue(VALID, Registration.class);
        SheetRowMapper mapper = new SheetRowMapper(json);
        StoredRegistration stored = new StoredRegistration("TA-TEST01", "hash", "2026-11-18 10:00:00", "2026-11-18 10:00:00", reg);
        List<Object> row = mapper.toRow(stored);
        assertThat(row).hasSize(SheetRowMapper.HEADERS.size());
        assertThat(row.get(11)).isEqualTo("其他：雪梨");
        assertThat(row.get(14)).isEqualTo("部分日期：07/16-07/19");
        assertThat(row.get(17)).isEqualTo("美食體驗、其他：手作");
        List<Object> asStrings = row.stream().map(o -> (Object) o.toString()).toList();
        assertThat(mapper.fromRow(asStrings)).isEqualTo(stored);
    }
}
