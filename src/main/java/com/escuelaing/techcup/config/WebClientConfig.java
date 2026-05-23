package com.escuelaing.techcup.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${services.gateway.url}")
    private String gatewayUrl;

    /**
     * WebClient apuntando al gateway.
     * Toda comunicación entre microservicios pasa por el gateway.
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(gatewayUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
