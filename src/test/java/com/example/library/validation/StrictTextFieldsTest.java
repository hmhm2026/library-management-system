package com.example.library.validation;

import com.example.library.config.JacksonConfig;
import com.example.library.dto.BookRequest;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** {"title": 1984} (a JSON number) must be rejected, {"title": "1984"} (a string) accepted. */
class StrictTextFieldsTest {

    private static ObjectMapper mapper;

    @BeforeAll
    static void setUp() {
        Jackson2ObjectMapperBuilder builder = Jackson2ObjectMapperBuilder.json();
        new JacksonConfig().strictTextFields().customize(builder);
        mapper = builder.build();
    }

    private static String book(String titleJson) {
        return "{\"title\":" + titleJson + ",\"author\":\"Frank Herbert\",\"publicationYear\":1965,\"price\":120}";
    }

    @Test
    void acceptsTheTitleAsAString() throws Exception {
        BookRequest b = mapper.readValue(book("\"1984\""), BookRequest.class);
        assertThat(b.getTitle()).isEqualTo("1984");
    }

    @Test
    void rejectsTheTitleAsAJsonInteger() {
        assertThatThrownBy(() -> mapper.readValue(book("1984"), BookRequest.class)).isInstanceOf(JsonMappingException.class);
    }

    @Test
    void rejectsDecimalsAndBooleansWhereTextIsExpected() {
        assertThatThrownBy(() -> mapper.readValue(book("19.84"), BookRequest.class)).isInstanceOf(JsonMappingException.class);
        assertThatThrownBy(() -> mapper.readValue(book("true"), BookRequest.class)).isInstanceOf(JsonMappingException.class);
    }

    @Test
    void numericFieldsStillAcceptNumbers() throws Exception {
        BookRequest b = mapper.readValue(book("\"Dune\""), BookRequest.class);
        assertThat(b.getPublicationYear()).isEqualTo(1965);
        assertThat(b.getPrice()).isEqualTo(120.0);
    }
}
