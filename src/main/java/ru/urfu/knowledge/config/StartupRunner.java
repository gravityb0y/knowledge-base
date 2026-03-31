package ru.urfu.knowledge.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.urfu.knowledge.service.FileProcessorService;
import ru.urfu.knowledge.service.WeaviateService;

@Configuration
public class StartupRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupRunner.class);

    @Bean
    CommandLineRunner run(FileProcessorService processorService, WeaviateService weaviateService) {
        return args -> {
//            List<KnowledgeChunk> chunks = processorService.process();
//            weaviateService.saveChunks(chunks);
            log.info(weaviateService.generateAnswer("что такое ИТС?"));
        };
    }

}
