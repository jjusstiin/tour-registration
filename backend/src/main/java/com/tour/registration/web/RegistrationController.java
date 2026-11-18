package com.tour.registration.web;

import com.tour.registration.model.Registration;
import com.tour.registration.model.StoredRegistration;
import com.tour.registration.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 報名人用的 API。
 * 送出後會拿到 id + editToken，前端存在瀏覽器裡，之後帶 X-Edit-Token 就能讀回或修改自己的報名。
 */
@RestController
public class RegistrationController {

    private final RegistrationService service;

    public RegistrationController(RegistrationService service) {
        this.service = service;
    }

    /** 給前端「叫醒」Render 免費主機用，也給 Render 做健康檢查。 */
    @GetMapping({"/", "/api/health"})
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    @PostMapping("/api/registrations")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistrationService.Created create(@Valid @RequestBody Registration registration) {
        return service.create(registration);
    }

    @GetMapping("/api/registrations/{id}")
    public MyRegistration get(@PathVariable String id,
                              @RequestHeader(value = "X-Edit-Token", required = false) String token) {
        StoredRegistration s = service.get(id, token);
        return new MyRegistration(s.id(), s.submittedAt(), s.updatedAt(), s.data());
    }

    @PutMapping("/api/registrations/{id}")
    public Map<String, String> update(@PathVariable String id,
                                      @RequestHeader(value = "X-Edit-Token", required = false) String token,
                                      @Valid @RequestBody Registration registration) {
        service.update(id, token, registration);
        return Map.of("id", id);
    }

    public record MyRegistration(String id, String submittedAt, String updatedAt, Registration registration) {
    }
}
