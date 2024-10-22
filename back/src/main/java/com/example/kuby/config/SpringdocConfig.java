package com.example.kuby.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


    @OpenAPIDefinition
    @Configuration
    @SecurityScheme(
            name = "Bearer Authentication",
            type = SecuritySchemeType.HTTP,
            bearerFormat = "JWT",
            scheme = "bearer"
    )
    public class SpringdocConfig {

        @Bean
        public OpenAPI baseOpenAPI() {
            return new OpenAPI()
                    .info(new Info()
                            .title("API")
                            .version("1.0.0")
                            .description("Spring doc"))
                    .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"));
        }


}
