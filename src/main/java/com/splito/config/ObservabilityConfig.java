package com.splito.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.splito.observability.HttpLoggingFilter;
import com.splito.observability.HttpLoggingProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

@Configuration
@EnableConfigurationProperties(HttpLoggingProperties.class)
public class ObservabilityConfig {

    @Bean
    public FilterRegistrationBean<HttpLoggingFilter> httpLoggingFilter(HttpLoggingProperties props,
                                                                       ObjectMapper objectMapper) {

        FilterRegistrationBean<HttpLoggingFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new HttpLoggingFilter(props, objectMapper));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 10); // early in chain
        return bean;
    }
}
