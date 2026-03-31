package ru.urfu.knowledge.service.processor;

import ru.urfu.knowledge.dto.KnowledgeChunk;

import java.util.List;

public interface ProcessorService {
    List<KnowledgeChunk> process();
}
