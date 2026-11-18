package com.tour.registration.web;

import com.tour.registration.model.Registration;
import com.tour.registration.model.StoredRegistration;
import com.tour.registration.service.RegistrationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

/** 主辦方用：看全部報名 + 統計。需要 X-Admin-Token（= 環境變數 ADMIN_TOKEN）。 */
@RestController
public class AdminController {

    private final RegistrationService service;
    private final String adminToken;

    public AdminController(RegistrationService service, @Value("${app.admin-token}") String adminToken) {
        this.service = service;
        this.adminToken = adminToken;
    }

    @GetMapping("/api/admin/registrations")
    public AdminView list(@RequestHeader(value = "X-Admin-Token", required = false) String token) {
        checkAdmin(token);
        List<StoredRegistration> all = service.listAll();
        List<AdminRow> rows = all.stream()
                .map(s -> new AdminRow(s.id(), s.submittedAt(), s.updatedAt(), s.data()))
                .toList();
        return new AdminView(RegistrationService.stats(all), rows);
    }

    private void checkAdmin(String token) {
        if (adminToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "伺服器尚未設定 ADMIN_TOKEN");
        }
        if (token == null || !MessageDigest.isEqual(
                token.getBytes(StandardCharsets.UTF_8), adminToken.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "管理密碼錯誤");
        }
    }

    public record AdminRow(String id, String submittedAt, String updatedAt, Registration registration) {
    }

    public record AdminView(RegistrationService.Stats stats, List<AdminRow> rows) {
    }
}
