package ru.urfu.knowledge.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.urfu.knowledge.dto.KnowledgeChunk;
import ru.urfu.knowledge.util.ChunkUtils;

import java.util.List;

@Service
public class YouTrackProcessorService implements ProcessorService {

    private final YouTrackService youTrackService;

    @Value("${youtrack.base-url}")
    private String baseUrl;

    @Autowired
    public YouTrackProcessorService(YouTrackService youTrackService) {
        this.youTrackService = youTrackService;
    }

    @Override
    public List<KnowledgeChunk> process() {
        return youTrackService.getAllIssues(40)
                .stream()
                .map( yti ->KnowledgeChunk.builder()
                        .title(yti.getSummary())
                        .content(ChunkUtils.normalizeText(yti.getDescription()))
                        .sourceName("youtrack")
                        .sourceId(yti.getIdReadable())
                        .url(baseUrl + "/issue/" + yti.getIdReadable())
                        .sourceType("task")
                        .sourceHash(ChunkUtils.getContentHash(yti.getDescription()))
                        .build())
                .toList();
    }
}
