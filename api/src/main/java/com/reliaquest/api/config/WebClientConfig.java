package com.reliaquest.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;

@Configuration
@EnableRetry
public class WebClientConfig {

    @Bean
    public WebClient webClient(Builder builder) {
        return builder.baseUrl("http://localhost:8112/api/v1/employee").build();
    }
}
