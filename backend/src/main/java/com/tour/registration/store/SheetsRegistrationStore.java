package com.tour.registration.store;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.AddSheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.tour.registration.model.StoredRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 用 Google 試算表當資料庫。每筆報名一列，第一列是標題。
 * 寫入一律用 RAW 模式：使用者輸入的「=...」不會被當成公式執行，電話開頭的 0 也不會被吃掉。
 */
public class SheetsRegistrationStore implements RegistrationStore {

    private static final Logger log = LoggerFactory.getLogger(SheetsRegistrationStore.class);

    private final Sheets sheets;
    private final String spreadsheetId;
    private final String tab;
    private final SheetRowMapper mapper;
    private final String lastColumn;
    private volatile boolean ready;

    public SheetsRegistrationStore(Sheets sheets, String spreadsheetId, String tab, SheetRowMapper mapper) {
        this.sheets = sheets;
        this.spreadsheetId = spreadsheetId;
        this.tab = tab;
        this.mapper = mapper;
        this.lastColumn = columnLetter(SheetRowMapper.HEADERS.size());
    }

    @Override
    public List<StoredRegistration> findAll() {
        try {
            ensureReady();
            List<List<Object>> values = sheets.spreadsheets().values()
                    .get(spreadsheetId, range("A2:" + lastColumn))
                    .execute().getValues();
            List<StoredRegistration> result = new ArrayList<>();
            if (values != null) {
                for (List<Object> row : values) {
                    if (!row.isEmpty() && !row.get(0).toString().isBlank()) {
                        result.add(mapper.fromRow(row));
                    }
                }
            }
            return result;
        } catch (IOException e) {
            throw new StoreException("讀取試算表失敗", e);
        }
    }

    @Override
    public synchronized void insert(StoredRegistration registration) {
        try {
            ensureReady();
            sheets.spreadsheets().values()
                    .append(spreadsheetId, range("A1"), body(mapper.toRow(registration)))
                    .setValueInputOption("RAW")
                    .setInsertDataOption("INSERT_ROWS")
                    .execute();
        } catch (IOException e) {
            throw new StoreException("寫入試算表失敗", e);
        }
    }

    @Override
    public synchronized void update(StoredRegistration registration) {
        try {
            ensureReady();
            int row = findRowNumber(registration.id());
            sheets.spreadsheets().values()
                    .update(spreadsheetId, range("A" + row + ":" + lastColumn + row), body(mapper.toRow(registration)))
                    .setValueInputOption("RAW")
                    .execute();
        } catch (IOException e) {
            throw new StoreException("更新試算表失敗", e);
        }
    }

    /** 找出這筆報名在試算表的第幾列（每次重新找，主辦方手動排序或刪列也不會錯位）。 */
    private int findRowNumber(String id) throws IOException {
        List<List<Object>> ids = sheets.spreadsheets().values()
                .get(spreadsheetId, range("A2:A")).execute().getValues();
        if (ids != null) {
            for (int i = 0; i < ids.size(); i++) {
                if (!ids.get(i).isEmpty() && id.equals(ids.get(i).get(0).toString())) {
                    return i + 2;
                }
            }
        }
        throw new IllegalStateException("試算表裡找不到報名 " + id);
    }

    /** 第一次使用時：分頁不存在就建立，標題列是空的就寫入標題。 */
    private synchronized void ensureReady() throws IOException {
        if (ready) {
            return;
        }
        Spreadsheet ss = sheets.spreadsheets().get(spreadsheetId)
                .setFields("sheets.properties.title").execute();
        boolean exists = ss.getSheets().stream()
                .anyMatch(s -> tab.equals(s.getProperties().getTitle()));
        if (!exists) {
            log.info("建立分頁「{}」", tab);
            sheets.spreadsheets().batchUpdate(spreadsheetId, new BatchUpdateSpreadsheetRequest().setRequests(List.of(
                    new Request().setAddSheet(new AddSheetRequest()
                            .setProperties(new SheetProperties().setTitle(tab)))))).execute();
        }
        List<List<Object>> header = sheets.spreadsheets().values()
                .get(spreadsheetId, range("A1:" + lastColumn + "1")).execute().getValues();
        if (header == null || header.isEmpty()) {
            log.info("寫入標題列");
            sheets.spreadsheets().values()
                    .update(spreadsheetId, range("A1"), body(new ArrayList<>(SheetRowMapper.HEADERS)))
                    .setValueInputOption("RAW")
                    .execute();
        }
        ready = true;
    }

    private String range(String a1) {
        return "'" + tab.replace("'", "''") + "'!" + a1;
    }

    private static ValueRange body(List<Object> row) {
        return new ValueRange().setValues(List.of(row));
    }

    /** 1 → A, 26 → Z, 27 → AA */
    static String columnLetter(int n) {
        StringBuilder sb = new StringBuilder();
        while (n > 0) {
            int r = (n - 1) % 26;
            sb.insert(0, (char) ('A' + r));
            n = (n - 1) / 26;
        }
        return sb.toString();
    }
}
