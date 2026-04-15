package com.splito.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI splitoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Splito API")
                        .description("REST API for Splito – split bills, track expenses, and balances")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("Splito Team")
                                .email("support@splito.com")
                        )
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")
                        )
                );
    }
}


