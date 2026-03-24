package ru.urfu.knowledge.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.urfu.knowledge.service.FileProcessorService;

@Configuration
public class StartupRunner {

    @Bean
    CommandLineRunner run(FileProcessorService service) {
        return args -> service.process();
    }

}
