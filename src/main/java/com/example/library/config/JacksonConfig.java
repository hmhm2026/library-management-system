package com.example.library.config;

import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.type.LogicalType;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    /**
     * By default Jackson quietly turns a JSON number into text, so {"title": 1984} would be stored as "1984".
     * This makes every String field strict: a number, decimal or boolean sent where text is expected
     * is rejected (HTTP 400, "Invalid Request Body"). Send {"title": "1984"} with quotes instead.
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer strictTextFields() {
        return builder -> builder.postConfigurer(mapper ->
                mapper.coercionConfigFor(LogicalType.Textual)
                        .setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)
                        .setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
                        .setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail));
    }
}
