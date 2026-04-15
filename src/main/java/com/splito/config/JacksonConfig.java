package com.splito.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {
    @Bean
    public ObjectMapper objectMapper() {
        // registers JavaTimeModule etc if present on classpath
        return new ObjectMapper().findAndRegisterModules();
    }
}