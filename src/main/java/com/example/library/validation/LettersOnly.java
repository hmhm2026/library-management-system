package com.example.library.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The text may contain letters only (any language, e.g. English or Arabic),
 * with single spaces between words.
 *
 * Rejected: digits, special characters, tabs, leading/trailing/double spaces,
 * empty text and text made of spaces only.
 *
 * A null value is accepted so optional fields stay optional;
 * combine with @NotBlank when the field is required.
 */
@Documented
@Constraint(validatedBy = LettersOnlyValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface LettersOnly {

    String message() default "Only letters are allowed, with single spaces between words (no numbers or special characters)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
