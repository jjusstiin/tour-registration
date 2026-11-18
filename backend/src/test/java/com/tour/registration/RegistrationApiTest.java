package com.tour.registration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tour.registration.store.SheetRowMapper;
import com.tour.registration.model.StoredRegistration;
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
            {"nameZh":"陳大明","surname":"CHEN","given":"TA MING","gender":"M","dob":"1980-01-02",
             "nationality":"台灣","passportNo":"A1234567","passportExp":"2031-05-06","visa":"need",
             "phone":"0912345678","email":"chen@example.com","im":"","address":"",
             "emName":"陳太太","emRel":"配偶","emPhone":"0922000000",
             "room":"twin","roommate":"","diet":"none","health":"","insurance":"self",
             "companions":[{"surname":"CHEN","given":"XIAO MEI","gender":"F","dob":"2010-03-04",
                            "passportNo":"B7654321","passportExp":"2030-01-01","relation":"女兒","nameZh":"陳小美"}],
             "notes":"=HYPERLINK(\\"x\\")","consent":true,"lang":"zh"}
            """;

    @Test
    void fullFlow() throws Exception {
        String body = mvc.perform(post("/api/registrations").contentType(MediaType.APPLICATION_JSON).content(VALID))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode created = json.readTree(body);
        String id = created.get("id").asText();
        String token = created.get("editToken").asText();
        assertThat(id).startsWith("AU-");

        mvc.perform(get("/api/registrations/" + id).header("X-Edit-Token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registration.surname").value("CHEN"))
                .andExpect(jsonPath("$.registration.companions[0].given").value("XIAO MEI"));

        mvc.perform(get("/api/registrations/" + id).header("X-Edit-Token", "wrong"))
                .andExpect(status().isNotFound());

        mvc.perform(put("/api/registrations/" + id).header("X-Edit-Token", token)
                        .contentType(MediaType.APPLICATION_JSON).content(VALID.replace("\"twin\"", "\"single\"")))
                .andExpect(status().isOk());

        mvc.perform(get("/api/admin/registrations"))
                .andExpect(status().isUnauthorized());

        mvc.perform(get("/api/admin/registrations").header("X-Admin-Token", "test-admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stats.registrations").value(1))
                .andExpect(jsonPath("$.stats.travellers").value(2))
                .andExpect(jsonPath("$.stats.room.single").value(1))
                .andExpect(jsonPath("$.rows[0].registration.room").value("single"));
    }

    @Test
    void rejectsMissingFieldsAndNoConsent() throws Exception {
        mvc.perform(post("/api/registrations").contentType(MediaType.APPLICATION_JSON)
                        .content(VALID.replace("\"surname\":\"CHEN\",\"given\"", "\"surname\":\"\",\"given\"")))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/registrations").contentType(MediaType.APPLICATION_JSON)
                        .content(VALID.replace("\"consent\":true", "\"consent\":false")))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/registrations").contentType(MediaType.APPLICATION_JSON)
                        .content(VALID.replace("\"room\":\"twin\"", "\"room\":\"penthouse\"")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sheetRowRoundTrip() throws Exception {
        var reg = json.readValue(VALID, com.tour.registration.model.Registration.class);
        var mapper = new SheetRowMapper(json);
        var stored = new StoredRegistration("AU-TEST01", "hash", "2026-11-18 10:00:00", "2026-11-18 10:00:00", reg);
        List<Object> row = mapper.toRow(stored);
        assertThat(row).hasSize(SheetRowMapper.HEADERS.size());
        assertThat(row.get(21)).isEqualTo("雙人房");
        // 模擬從試算表讀回來（全部變成字串）
        List<Object> asStrings = row.stream().map(o -> (Object) o.toString()).toList();
        assertThat(mapper.fromRow(asStrings)).isEqualTo(stored);
    }
}
