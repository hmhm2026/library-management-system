package com.example.library.validation;

import com.example.library.dto.BookRequest;
import com.example.library.dto.CategoryRequest;
import com.example.library.dto.CreateAdminRequest;
import com.example.library.dto.CreateReviewRequest;
import com.example.library.dto.RegisterRequest;
import com.example.library.dto.ReviewRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/** Runs the real Bean Validation engine against every DTO that accepts free text. */
class DtoValidationTest {

    private static Validator validator;

    // digits-only values such as "1984" are rejected everywhere except the book title
    private static final String[] BAD_TEXT = {"Ann123", "1984", "@@@###", "Ann@", "   ", "", " Ann", "Ann  Ali"};

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private static Set<String> invalidFields(Object dto) {
        return validator.validate(dto).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    // ---------- valid objects ----------

    private static RegisterRequest validRegister() {
        RegisterRequest r = new RegisterRequest();
        r.setFullName("Ann Ali");
        r.setEmail("ann@example.com");
        r.setPassword("secret123");
        return r;
    }

    private static BookRequest validBook() {
        BookRequest b = new BookRequest();
        b.setTitle("Dune");
        b.setAuthor("Frank Herbert");
        b.setPublicationYear(1965);
        b.setPrice(120.0);
        return b;
    }

    @Test
    void validObjectsPass() {
        assertThat(validator.validate(validRegister())).isEmpty();
        assertThat(validator.validate(validBook())).isEmpty();

        BookRequest full = validBook();
        full.setPublisher("Chilton Books");
        full.setGenre("Science Fiction");
        full.setDescription("A desert planet and a young heir");
        assertThat(validator.validate(full)).isEmpty();
    }

    // ---------- register / create admin ----------

    @ParameterizedTest
    @ValueSource(strings = {"Ann123", "@@@###", "Ann@", "   ", "", " Ann", "Ann  Ali"})
    void registerRejectsBadFullName(String value) {
        RegisterRequest r = validRegister();
        r.setFullName(value);
        assertThat(invalidFields(r)).contains("fullName");
    }

    @Test
    void registerStillRequiresFullName() {
        RegisterRequest r = validRegister();
        r.setFullName(null);
        assertThat(invalidFields(r)).contains("fullName");
    }

    @Test
    void registerStillChecksEmailAndPassword() {
        RegisterRequest r = validRegister();
        r.setEmail("not-an-email");
        r.setPassword("123");
        assertThat(invalidFields(r)).contains("email", "password");
    }

    @Test
    void createAdminRejectsBadFullName() {
        for (String bad : BAD_TEXT) {
            CreateAdminRequest r = new CreateAdminRequest();
            r.setFullName(bad);
            r.setEmail("boss@example.com");
            r.setPassword("secret123");
            assertThat(invalidFields(r)).as("fullName=\"%s\"", bad).contains("fullName");
        }
    }

    // ---------- books ----------

    @ParameterizedTest
    @ValueSource(strings = {"1984", "Dune 2", "Fahrenheit 451", "Dune"})
    void bookAcceptsTitlesWithLettersAndNumbers(String value) {
        BookRequest b = validBook();
        b.setTitle(value);
        assertThat(validator.validate(b)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Dune!", "   ", "", "Catch-22", "@@@###", " 1984", "Dune  2"})
    void bookRejectsBadTitle(String value) {
        BookRequest b = validBook();
        b.setTitle(value);
        assertThat(invalidFields(b)).contains("title");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Frank99", "1984", "F. Herbert", "   ", ""})
    void bookRejectsBadAuthor(String value) {
        BookRequest b = validBook();
        b.setAuthor(value);
        assertThat(invalidFields(b)).contains("author");
    }

    @Test
    void bookOptionalTextFieldsAcceptNullButRejectBadText() {
        BookRequest ok = validBook(); // publisher, genre, description are all null
        assertThat(validator.validate(ok)).isEmpty();

        for (String bad : BAD_TEXT) {
            BookRequest b = validBook();
            b.setPublisher(bad);
            b.setGenre(bad);
            b.setDescription(bad);
            assertThat(invalidFields(b)).as("optional text=\"%s\"", bad).contains("publisher", "genre", "description");
        }
    }

    @Test
    void bookStillChecksNonTextFields() {
        BookRequest b = validBook();
        b.setPrice(-5.0);
        b.setPublicationYear(null);
        assertThat(invalidFields(b)).contains("price", "publicationYear");
    }

    // ---------- categories ----------

    @Test
    void categoryRejectsBadNameAndDescription() {
        for (String bad : BAD_TEXT) {
            CategoryRequest c = new CategoryRequest();
            c.setName(bad);
            c.setDescription(bad);
            assertThat(invalidFields(c)).as("category text=\"%s\"", bad).contains("name", "description");
        }
        CategoryRequest ok = new CategoryRequest();
        ok.setName("Science Fiction");
        assertThat(validator.validate(ok)).isEmpty();
    }

    // ---------- reviews ----------

    @Test
    void reviewCommentMustBeLettersOnlyWhenPresent() {
        for (String bad : BAD_TEXT) {
            CreateReviewRequest create = new CreateReviewRequest();
            create.setBookId(1L);
            create.setRating(5);
            create.setComment(bad);
            assertThat(invalidFields(create)).as("create comment=\"%s\"", bad).contains("comment");

            ReviewRequest update = new ReviewRequest();
            update.setRating(4);
            update.setComment(bad);
            assertThat(invalidFields(update)).as("update comment=\"%s\"", bad).contains("comment");
        }
        ReviewRequest noComment = new ReviewRequest();
        noComment.setRating(4);
        assertThat(validator.validate(noComment)).isEmpty(); // the comment is optional
    }
}
