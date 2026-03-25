package ru.urfu.knowledge.service;

import io.weaviate.client6.v1.api.WeaviateClient;
import io.weaviate.client6.v1.api.collections.CollectionHandle;
import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.api.collections.data.InsertManyResponse;
import org.apache.commons.collections4.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.urfu.knowledge.entity.KnowledgeChunk;
import ru.urfu.knowledge.mapper.ChunkMapper;

import java.util.List;
import java.util.Map;

@Service
public class WeaviateService {

    private static final Logger log = LoggerFactory.getLogger(WeaviateService.class);
    private final WeaviateClient weaviateClient;

    @Value("${weaviate.collection.name}")
    private String collectionName;

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
}
