package com.crms.integration.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * REST template configuration for external API integrations.
 */
@Configuration
public class HmrcOAuth2Config {

    @Bean
    public RestTemplate hmrcRestTemplate(RestTemplateBuilder builder, IntegrationProperties props) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(30));
        factory.setReadTimeout(Duration.ofSeconds(60));
        
        return builder
                .requestFactory(() -> factory)
                .build();
    }

    @Bean
    public RestTemplate companiesHouseRestTemplate(RestTemplateBuilder builder, IntegrationProperties props) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(props.getCompaniesHouse().getConnectionTimeout()));
        factory.setReadTimeout(Duration.ofMillis(props.getCompaniesHouse().getReadTimeout()));
        
        return builder
                .requestFactory(() -> factory)
                .build();
    }

    @Bean
    public RestTemplate cscsRestTemplate(RestTemplateBuilder builder, IntegrationProperties props) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(props.getCscs().getConnectionTimeout()));
        factory.setReadTimeout(Duration.ofMillis(props.getCscs().getReadTimeout()));
        
        return builder
                .requestFactory(() -> factory)
                .build();
    }
}
