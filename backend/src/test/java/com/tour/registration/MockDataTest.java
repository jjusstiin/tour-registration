package com.tour.registration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** MOCK_DATA=true 時，12 筆假資料都要通過驗證並出現在主辦方頁。 */
@SpringBootTest(properties = {"app.admin-token=t", "app.mock-data=true"})
@AutoConfigureMockMvc
class MockDataTest {

    @Autowired
    MockMvc mvc;

    @Test
    void seedsMockRegistrations() throws Exception {
        mvc.perform(get("/api/admin/registrations").header("X-Admin-Token", "t"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stats.registrations").value(12))
                .andExpect(jsonPath("$.rows[0].id").value("TA-MOCK01"))
                .andExpect(jsonPath("$.stats.breakdown.region.taichung").value(4))
                .andExpect(jsonPath("$.stats.breakdown.attendance.partial").value(5));
    }
}
