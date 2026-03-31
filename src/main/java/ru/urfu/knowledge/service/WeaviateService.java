package ru.urfu.knowledge.service;

import io.weaviate.client6.v1.api.WeaviateClient;
import io.weaviate.client6.v1.api.collections.CollectionHandle;
import io.weaviate.client6.v1.api.collections.data.InsertManyResponse;
import io.weaviate.client6.v1.api.collections.generate.GenerativeProvider;
import org.apache.commons.collections4.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.urfu.knowledge.dto.KnowledgeChunk;
import ru.urfu.knowledge.mapper.ChunkMapper;

import java.util.List;
import java.util.Map;

@Service
public class WeaviateService {

    private static final Logger log = LoggerFactory.getLogger(WeaviateService.class);
    private final WeaviateClient weaviateClient;

    @Value("${weaviate.collection.name}")
    private String collectionName;

    @Value("${ollama.api-endpoint}")
    private String apiEndpoint;

    @Value("${ollama.generative-model}")
    private String generativeModel;

    @Autowired
    public WeaviateService(WeaviateClient weaviateClient) {
        this.weaviateClient = weaviateClient;
    }

    public void saveChunks(List<KnowledgeChunk> chunks) {
        CollectionHandle<Map<String, Object>> collection = weaviateClient.collections.use(collectionName);

        List<Map<String, Object>> toInsert = chunks.stream()
                .map(ChunkMapper::toMap)
                .toList();
        for (List<Map<String, Object>> subList : ListUtils.partition(toInsert, 10)) {
            InsertManyResponse insertResponse = collection.data.insertMany(subList.toArray(Map[]::new));
            if (!insertResponse.errors().isEmpty()) {
                for (String error : insertResponse.errors()) {
                    log.error("Ошибка при вставке чанков: {}", error);
                }
            }
        }

        log.info("Вставка чанков завершена");
    }

    public String generateAnswer(String question) {
        CollectionHandle<Map<String, Object>> collection = weaviateClient.collections.use(collectionName);
        var response = collection.generate.nearText(question, q -> q.limit(5).returnProperties("title", "content"),
                g -> g.groupedTask("""
                                Ты — эксперт по учебным информационным системам УрФУ.
                                
                                Твоя задача — ответить на вопрос пользователя, используя ТОЛЬКО предоставленный контекст.
                                
                                Правила:
                                1. Отвечай строго на русском языке
                                2. НЕ используй слова на других языках
                                3. НЕ придумывай информацию, которой нет в контексте
                                4. Если информации недостаточно — напиши: "Недостаточно данных"
                                5. Отвечай кратко и по делу (3–5 предложений)
                                
                                Вопрос:
                                """ + question,
                        p -> p.generativeProvider(GenerativeProvider.ollama(o -> o.
                                apiEndpoint(apiEndpoint)
                                .model(generativeModel)))));
        return response.generative().text();
    }
}
