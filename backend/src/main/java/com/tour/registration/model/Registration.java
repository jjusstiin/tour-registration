package com.tour.registration.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/** 一筆報名（報名人本人 + 同行者）。選項欄位存代碼，例如 gender = "M"。 */
public record Registration(
        @Size(max = 50) String nameZh,
        @NotBlank @Size(max = 60) String surname,
        @NotBlank @Size(max = 60) String given,
        @NotBlank @Pattern(regexp = Options.GENDER_RE) String gender,
        @NotBlank @Pattern(regexp = Options.DATE_RE) String dob,
        @NotBlank @Size(max = 60) String nationality,

        @NotBlank @Size(max = 30) String passportNo,
        @NotBlank @Pattern(regexp = Options.DATE_RE) String passportExp,
        @NotBlank @Pattern(regexp = Options.VISA_RE) String visa,

        @NotBlank @Size(max = 30) String phone,
        @NotBlank @Email @Size(max = 120) String email,
        @Size(max = 60) String im,
        @Size(max = 200) String address,

        @NotBlank @Size(max = 50) String emName,
        @NotBlank @Size(max = 30) String emRel,
        @NotBlank @Size(max = 30) String emPhone,

        @NotBlank @Pattern(regexp = Options.ROOM_RE) String room,
        @Size(max = 60) String roommate,
        @NotBlank @Pattern(regexp = Options.DIET_RE) String diet,
        @Size(max = 1000) String health,
        @NotBlank @Pattern(regexp = Options.INSURANCE_RE) String insurance,

        @Size(max = 10) List<@Valid Companion> companions,

        @Size(max = 2000) String notes,
        @AssertTrue boolean consent,
        @Pattern(regexp = "zh|en") String lang
) {
    public Registration {
        companions = companions == null ? List.of() : List.copyOf(companions);
    }

    public int travellerCount() {
        return 1 + companions.size();
    }
}
