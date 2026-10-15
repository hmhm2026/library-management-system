package com.example.library.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class LettersOnlyValidatorTest {

    private final LettersOnlyValidator validator = new LettersOnlyValidator();

    @ParameterizedTest
    @ValueSource(strings = {"Ann", "Ann Ali", "Frank Herbert", "أحمد محمد", "مُحَمَّد", "Émile Zola", "Ann أحمد"})
    void acceptsLettersWithSingleSpacesBetweenWords(String value) {
        assertThat(validator.isValid(value, null)).isTrue();
    }

    @Test
    void acceptsNullSoOptionalFieldsStayOptional() {
        assertThat(validator.isValid(null, null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "", "   ", " ", "\t",                       // empty or spaces only
            "12345", "Ann123", "An1n", "أحمد ١٢٣",       // digits (Latin and Arabic-Indic)
            "@@@###", "Ann@", "O'Brien", "Jean-Luc", "Dr. Ann", "Ann, Ali",   // special characters
            " Ann", "Ann ", "Ann  Ali",                // leading, trailing and double spaces
            "Ann\nAli", "Ann\u00A0Ali", "An\u200Dn",   // newline, non-breaking space, zero-width joiner
            "\u064E"                                   // a diacritic mark with no letter
    })
    void rejectsDigitsSpecialCharactersAndBlankText(String value) {
        assertThat(validator.isValid(value, null)).isFalse();
    }
}
