package ru.urfu.knowledge.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.tika.Tika;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "docs")
@Getter
@Setter
public class DocsConfiguration {
    private String path;

    @Bean
    public Tika tika() {
        return new Tika();
    }

}
