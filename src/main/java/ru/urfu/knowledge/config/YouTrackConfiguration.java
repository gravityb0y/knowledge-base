package ru.urfu.knowledge.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
public class YouTrackConfiguration {

    @Value("${youtrack.base-url}")
    private String baseUrl;

    @Value("${youtrack.auth-token}")
    private String token;

    @Bean
    public RestClient youTrackRestClient() {
        return RestClient.builder()
                .baseUrl(baseUrl + "/api")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
