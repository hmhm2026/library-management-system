package com.example.library.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class LettersAndDigitsValidatorTest {

    private final LettersAndDigitsValidator validator = new LettersAndDigitsValidator();

    @ParameterizedTest
    @ValueSource(strings = {"1984", "Dune", "Dune 2", "Fahrenheit 451", "2001 A Space Odyssey", "كتاب 2", "١٩٨٤", "Émile 3"})
    void acceptsLettersAndNumbersWithSingleSpaces(String value) {
        assertThat(validator.isValid(value, null)).isTrue();
    }

    @Test
    void acceptsNull() {
        assertThat(validator.isValid(null, null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "", "   ", " ", "\t",             // empty or spaces only
            "@@@###", "Dune!", "Catch-22", "Dr. No", "Ann, Ali",   // special characters
            " 1984", "1984 ", "Dune  2",     // leading, trailing and double spaces
            "1984\n", "Dune\u00A02", "\u064E"  // newline, non-breaking space, a diacritic with no letter
    })
    void rejectsSpecialCharactersAndBlankText(String value) {
        assertThat(validator.isValid(value, null)).isFalse();
    }
}
