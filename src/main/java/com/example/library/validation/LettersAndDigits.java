package com.example.library.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used for the book title only. The text may contain letters and numbers
 * (so "1984", "Dune 2" and "Fahrenheit 451" are valid titles), with single spaces
 * between words.
 *
 * Rejected: special characters, empty text, text made of spaces only,
 * and leading, trailing or double spaces.
 *
 * A null value is accepted; combine with @NotBlank when the field is required.
 */
@Documented
@Constraint(validatedBy = LettersAndDigitsValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface LettersAndDigits {

    String message() default "Only letters and numbers are allowed, with single spaces between words (no special characters)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
