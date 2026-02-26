package com.example.demo;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class EmailListValidator implements ConstraintValidator<ValidEmailList, String> {

    // Hyphen at the start of the class avoids ambiguity with ranges.
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[-A-Za-z0-9+_.]+@[-A-Za-z0-9.]+\\.[A-Za-z]{2,}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // field is optional; use @NotBlank to require it
        }
        for (String address : value.split(",")) {
            if (!EMAIL_PATTERN.matcher(address.trim()).matches()) {
                return false;
            }
        }
        return true;
    }
}
