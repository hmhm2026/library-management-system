package com.example.library.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class LettersOnlyValidator implements ConstraintValidator<LettersOnly, String> {

    /**
     * \p{L} = a letter in any language, \p{M} = a combining mark (e.g. Arabic diacritics).
     * The lookahead guarantees at least one real letter, so marks alone are not enough.
     * Words are separated by exactly one ordinary space.
     */
    private static final Pattern PATTERN =
            Pattern.compile("^(?=.*\\p{L})[\\p{L}\\p{M}]+(?: [\\p{L}\\p{M}]+)*$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // optional fields stay optional; use @NotBlank to require a value
        }
        return PATTERN.matcher(value).matches();
    }
}
