package com.tour.registration.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class StoreConfig {

    private static final Logger log = LoggerFactory.getLogger(StoreConfig.class);

    @Bean
    public RegistrationStore registrationStore(
            @Value("${app.google.credentials-json}") String credentialsJson,
            @Value("${app.google.credentials-file}") String credentialsFile,
            @Value("${app.sheet.id}") String sheetId,
            @Value("${app.sheet.tab}") String tab,
            @Value("${app.mock-data}") boolean mockData,
            ObjectMapper objectMapper,
            Validator validator) throws Exception {

        boolean hasCredentials = !credentialsJson.isBlank() || !credentialsFile.isBlank();
        if (sheetId.isBlank() || !hasCredentials) {
            log.warn("====================================================================");
            log.warn("沒有設定 SHEET_ID 或 Google 金鑰 → 使用記憶體暫存，伺服器重開資料就會消失。");
            log.warn("====================================================================");
            InMemoryRegistrationStore store = new InMemoryRegistrationStore();
            if (mockData) {
                MockData.seed(store, objectMapper, validator);
            }
            return store;
        }
        // 連真的試算表時，就算設了 MOCK_DATA 也不會寫入假資料

        GoogleCredentials credentials;
        try (InputStream in = credentialsJson.isBlank()
                ? new FileInputStream(credentialsFile)
                : new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8))) {
            credentials = GoogleCredentials.fromStream(in).createScoped(List.of(SheetsScopes.SPREADSHEETS));
        }

        Sheets sheets = new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName("tour-registration")
                .build();

        log.info("使用 Google 試算表 {}，分頁「{}」", sheetId, tab);
        return new SheetsRegistrationStore(sheets, sheetId, tab, new SheetRowMapper(objectMapper));
    }
}
