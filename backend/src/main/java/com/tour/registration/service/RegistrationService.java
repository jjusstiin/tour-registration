package com.tour.registration.service;

import com.tour.registration.model.Registration;
import com.tour.registration.model.StoredRegistration;
import com.tour.registration.store.RegistrationStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class RegistrationService {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String ID_CHARS = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"; // 去掉容易看錯的 0/O、1/I/L
    private final SecureRandom random = new SecureRandom();
    private final RegistrationStore store;
    private final ZoneId zone;

    public RegistrationService(RegistrationStore store, @Value("${app.timezone}") String timezone) {
        this.store = store;
        this.zone = ZoneId.of(timezone);
    }

    public record Created(String id, String editToken) {
    }

    public record Stats(int registrations, int travellers,
                        Map<String, Integer> room, Map<String, Integer> diet,
                        Map<String, Integer> visa, Map<String, Integer> insurance) {
    }

    public Created create(Registration r) {
        String id = newId();
        String token = newToken();
        String now = now();
        store.insert(new StoredRegistration(id, hash(token), now, now, r));
        return new Created(id, token);
    }

    public StoredRegistration get(String id, String token) {
        StoredRegistration s = store.findById(id).orElseThrow(NotFoundException::new);
        if (token == null || !MessageDigest.isEqual(
                hash(token).getBytes(StandardCharsets.UTF_8),
                s.tokenHash().getBytes(StandardCharsets.UTF_8))) {
            throw new NotFoundException(); // 憑證錯誤也回「找不到」，不透露這筆是否存在
        }
        return s;
    }

    public synchronized void update(String id, String token, Registration r) {
        StoredRegistration old = get(id, token);
        store.update(new StoredRegistration(id, old.tokenHash(), old.submittedAt(), now(), r));
    }

    public List<StoredRegistration> listAll() {
        return store.findAll();
    }

    public static Stats stats(List<StoredRegistration> all) {
        Map<String, Integer> room = new TreeMap<>();
        Map<String, Integer> diet = new TreeMap<>();
        Map<String, Integer> visa = new TreeMap<>();
        Map<String, Integer> insurance = new TreeMap<>();
        int travellers = 0;
        for (StoredRegistration s : all) {
            Registration r = s.data();
            travellers += r.travellerCount();
            room.merge(r.room(), 1, Integer::sum);
            diet.merge(r.diet(), 1, Integer::sum);
            visa.merge(r.visa(), 1, Integer::sum);
            insurance.merge(r.insurance(), 1, Integer::sum);
        }
        return new Stats(all.size(), travellers,
                new LinkedHashMap<>(room), new LinkedHashMap<>(diet),
                new LinkedHashMap<>(visa), new LinkedHashMap<>(insurance));
    }

    private String now() {
        return ZonedDateTime.now(zone).format(TS);
    }

    private String newId() {
        StringBuilder sb = new StringBuilder("AU-");
        for (int i = 0; i < 6; i++) {
            sb.append(ID_CHARS.charAt(random.nextInt(ID_CHARS.length())));
        }
        return sb.toString();
    }

    private String newToken() {
        byte[] b = new byte[24];
        random.nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    static String hash(String token) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
