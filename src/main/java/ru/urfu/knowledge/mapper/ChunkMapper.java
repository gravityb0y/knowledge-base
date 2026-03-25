package ru.urfu.knowledge.mapper;

import ru.urfu.knowledge.entity.KnowledgeChunk;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChunkMapper {

    public static Map<String, Object> toMap(KnowledgeChunk chunk) {
        Map<String, Object> map = new HashMap<>();
        map.put("chunkId", generateUuid(chunk));

        map.put("title", chunk.getTitle());
        map.put("url", chunk.getUrl());
        map.put("content", chunk.getContent());

        map.put("sourceType", chunk.getSourceType());
        map.put("sourceId", chunk.getSourceId());
        map.put("sourceName", chunk.getSourceName());

        map.put("chunkIndex", chunk.getChunkIndex());

        map.put("sourceHash", chunk.getSourceHash());

        return map;
    }

    private static String generateUuid(KnowledgeChunk chunk) {
        String raw = chunk.getSourceId() + "_" + chunk.getChunkIndex();
        return UUID.nameUUIDFromBytes(raw.getBytes()).toString();
    }
}
