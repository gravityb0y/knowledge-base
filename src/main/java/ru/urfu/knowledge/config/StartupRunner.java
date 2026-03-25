package ru.urfu.knowledge.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.urfu.knowledge.entity.KnowledgeChunk;
import ru.urfu.knowledge.service.FileProcessorService;
import ru.urfu.knowledge.service.WeaviateService;

import java.util.List;

@Configuration
public class StartupRunner {

    @Bean
    CommandLineRunner run(FileProcessorService processorService, WeaviateService weaviateService) {
        return args -> {
            List<KnowledgeChunk> chunks = processorService.process();
            weaviateService.saveChunks(chunks);
        };
    }

}
