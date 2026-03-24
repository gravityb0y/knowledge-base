package ru.urfu.knowledge.service;

import ru.urfu.knowledge.entity.KnowledgeChunk;

import java.util.List;

public interface ProcessorService {
    List<KnowledgeChunk> process();
}
