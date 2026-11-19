package com.tour.registration.store;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tour.registration.model.Registration;
import com.tour.registration.model.StoredRegistration;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 本機試跑用的假資料（resources/mock/registrations.json）。
 * 只會在「記憶體模式 + MOCK_DATA=true」時載入，正式環境（Google 試算表）永遠不會用到。
 */
final class MockData {

    private static final Logger log = LoggerFactory.getLogger(MockData.class);
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private MockData() {
    }

    static void seed(RegistrationStore store, ObjectMapper json, Validator validator) {
        List<Registration> regs;
        try (InputStream in = MockData.class.getResourceAsStream("/mock/registrations.json")) {
            regs = json.readValue(in, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new IllegalStateException("讀取假資料失敗", e);
        }
        LocalDateTime t = LocalDateTime.of(2026, 11, 10, 9, 12);
        for (int i = 0; i < regs.size(); i++) {
            Registration r = regs.get(i);
            var problems = validator.validate(r);
            if (!problems.isEmpty()) {
                throw new IllegalStateException("假資料第 " + (i + 1) + " 筆不合格：" + problems);
            }
            String ts = t.plusHours(i * 17L).plusMinutes(i * 7L).format(TS);
            store.insert(new StoredRegistration(String.format("TA-MOCK%02d", i + 1), "mock", ts, ts, r));
        }
        log.info("已載入 {} 筆假資料（僅本機記憶體模式）", regs.size());
    }
}
