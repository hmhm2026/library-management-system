package com.example.library.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class LettersAndDigitsValidator implements ConstraintValidator<LettersAndDigits, String> {

    /**
     * \p{L} = a letter in any language, \p{Nd} = a decimal digit (including Arabic-Indic digits),
     * \p{M} = a combining mark (e.g. Arabic diacritics).
     * The lookahead needs at least one letter or digit, so marks alone are not enough.
     * Words are separated by exactly one ordinary space.
     */
    private static final Pattern PATTERN =
            Pattern.compile("^(?=.*[\\p{L}\\p{Nd}])[\\p{L}\\p{M}\\p{Nd}]+(?: [\\p{L}\\p{M}\\p{Nd}]+)*$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // use @NotBlank to require a value
        }
        return PATTERN.matcher(value).matches();
    }
}
