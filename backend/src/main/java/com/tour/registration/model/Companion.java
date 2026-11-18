package com.tour.registration.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** 同行者（由報名人代為填寫）。 */
public record Companion(
        @NotBlank @Size(max = 60) String surname,
        @NotBlank @Size(max = 60) String given,
        @NotBlank @Pattern(regexp = Options.GENDER_RE) String gender,
        @NotBlank @Pattern(regexp = Options.DATE_RE) String dob,
        @NotBlank @Size(max = 30) String passportNo,
        @NotBlank @Pattern(regexp = Options.DATE_RE) String passportExp,
        @NotBlank @Size(max = 30) String relation,
        @Size(max = 50) String nameZh
) {
}
