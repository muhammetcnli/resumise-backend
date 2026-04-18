package com.resumise.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI resumiseOpenApi() {
        SecurityScheme sessionCookieScheme = new SecurityScheme()
                .name("JSESSIONID")
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.COOKIE)
                .description("Session cookie set after OAuth2 login");

        return new OpenAPI()
                .info(new Info()
                        .title("Resumise Backend API")
                        .version("v1")
                        .description("Backend API for CV management and job analysis"))
                .addSecurityItem(new SecurityRequirement().addList("sessionCookie"))
                .schemaRequirement("sessionCookie", sessionCookieScheme);
    }
}

