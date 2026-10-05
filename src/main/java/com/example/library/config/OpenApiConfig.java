package com.example.library.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * بيضيف زرار "Authorize" في Swagger UI عشان تقدر تحط التوكن مرة واحدة
 * ويتبعت تلقائيًا مع كل الطلبات اللي بعدها من غير ما تكرره كل مرة.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Library Management API")
                        .version("1.0.0")
                        .description("A REST API for managing a library system — books, inventory, "
                                + "borrowing, and role-based access control (Admin / Client)."
                                + "\n\nPowered and developed by: Ibrahim Khamiss"
                                + "\nWhatsApp: +201014778296")
                        .contact(new Contact()
                                .name("Ibrahim Khamiss")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
