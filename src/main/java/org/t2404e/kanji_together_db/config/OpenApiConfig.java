package org.t2404e.kanji_together_db.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Kanji Together API",
                description = "APIs for question attempts, kanji mastery, and spaced repetition learning",
                version = "v1",
                contact = @Contact(
                        name = "Kanji Together Team",
                        email = "support@kanjitogether.com"
                )
        )
)
public class OpenApiConfig {
}
