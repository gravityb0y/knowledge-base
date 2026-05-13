package ru.urfu.knowledge.service;

import io.weaviate.client6.v1.api.collections.query.Filter;
import io.weaviate.client6.v1.api.WeaviateClient;
import io.weaviate.client6.v1.api.collections.CollectionHandle;
import io.weaviate.client6.v1.api.collections.data.InsertManyResponse;
import io.weaviate.client6.v1.api.collections.generate.GenerativeObject;
import io.weaviate.client6.v1.api.collections.generate.GenerativeProvider;
import org.apache.commons.collections4.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.urfu.knowledge.dto.KnowledgeChunk;
import ru.urfu.knowledge.dto.QuestionResponse;
import ru.urfu.knowledge.dto.SourceDto;
import ru.urfu.knowledge.mapper.ChunkMapper;

import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    public QuestionResponse generateAnswer(String question) {
        CollectionHandle<Map<String, Object>> collection = weaviateClient.collections.use(collectionName);

        var response = collection.generate.nearText(
                question,
                q -> q
                        .limit(5)
                        .returnProperties(
                                "title",
                                "content",
                                "sourceType",
                                "sourceName",
                                "url",
                                "chunkIndex"
                        ),
                g -> g.groupedTask("""
                    Ты — эксперт по учебным информационным системам УрФУ.
                    Твоя задача — ответить на вопрос пользователя, используя ТОЛЬКО предоставленный контекст.

                    Правила:
                    1. Отвечай строго на русском языке
                    2. Не используй сведения, которых нет в контексте
                    3. Если информации недостаточно — напиши: "Недостаточно данных"
                    4. Отвечай кратко и по делу
                    5. Не упоминай, что тебе был предоставлен контекст

                    Вопрос пользователя:
                    """ + question,
                        p -> p.generativeProvider(
                                GenerativeProvider.ollama(o -> o
                                        .apiEndpoint(apiEndpoint)
                                        .model(generativeModel)
                                )
                        )
                )
        );

        String answer = response.generative() == null || response.generative().text() == null
                ? "Недостаточно данных"
                : response.generative().text();

        List<SourceDto> sources = response.objects().stream()
                .map(GenerativeObject::properties)
                .filter(Objects::nonNull)
                .map(this::toSourceDto)
                .toList();

        return QuestionResponse.builder()
                .answer(answer)
                .sources(sources)
                .build();
    }

    private SourceDto toSourceDto(Map<String, Object> properties) {
        return SourceDto.builder()
                .title(asString(properties.get("title")))
                .sourceType(asString(properties.get("sourceType")))
                .sourceName(asString(properties.get("sourceName")))
                .url(asString(properties.get("url")))
                .fragment(cutFragment(asString(properties.get("content"))))
                .build();
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private String cutFragment(String content) {
        if (content == null) {
            return null;
        }

        int maxLength = 500;
        if (content.length() <= maxLength) {
            return content;
        }

        return content.substring(0, maxLength) + "...";
    }

    public void deleteBySourceType(String sourceType) {
        CollectionHandle<Map<String, Object>> collection = weaviateClient.collections.use(collectionName);

        var result = collection.data.deleteMany(
                Filter.property("sourceType").eq(sourceType)
        );

        log.info("Удалены старые чанки sourceType={}: {}", sourceType, result);
    }

    public void deleteBySourceName(String sourceName) {
        CollectionHandle<Map<String, Object>> collection = weaviateClient.collections.use(collectionName);

        var result = collection.data.deleteMany(
                Filter.property("sourceName").eq(sourceName)
        );

        log.info("Удалены старые чанки sourceName={}: {}", sourceName, result);
    }

    public void deleteDocuments() {
        deleteBySourceName("docs");
    }

    public void deleteYouTrack() {
        deleteBySourceName("youtrack");
    }
}
