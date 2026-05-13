package ru.urfu.knowledge.service.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.urfu.knowledge.dto.KnowledgeChunk;
import ru.urfu.knowledge.dto.YouTrackArticle;
import ru.urfu.knowledge.service.YouTrackService;
import ru.urfu.knowledge.util.ChunkUtils;
import ru.urfu.knowledge.util.Chunker;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Service
public class YouTrackArticleProcessorService {

    private final YouTrackService youTrackService;

    @Value("${youtrack.base-url}")
    private String baseUrl;

    @Autowired
    public YouTrackArticleProcessorService(YouTrackService youTrackService) {
        this.youTrackService = youTrackService;
    }

    public List<KnowledgeChunk> process() {
        return youTrackService.getAllArticles(40)
                .stream()
                .filter(article -> article.getContent() != null && !article.getContent().isBlank())
                .map(this::processArticle)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .toList();
    }

    private List<KnowledgeChunk> processArticle(YouTrackArticle article) {
        String normalizedContent = ChunkUtils.normalizeText(article.getContent());

        if (normalizedContent == null || normalizedContent.isBlank()) {
            return List.of();
        }

        String hash = ChunkUtils.getContentHash(normalizedContent);

        List<String> chunks = Chunker.chunkText(normalizedContent, 150, 2)
                .stream()
                .filter(ChunkUtils::isValidChunk)
                .toList();

        List<KnowledgeChunk> result = new LinkedList<>();

        for (int i = 0; i < chunks.size(); i++) {
            result.add(KnowledgeChunk.builder()
                    .title(article.getSummary())
                    .content(chunks.get(i))
                    .sourceName("youtrack")
                    .sourceId(resolveArticleId(article))
                    .sourceType("article")
                    .url(baseUrl + "articles/" + resolveArticleId(article))
                    .chunkIndex(i)
                    .sourceHash(hash)
                    .build());
        }

        return result;
    }

    private String resolveArticleId(YouTrackArticle article) {
        if (article.getIdReadable() != null && !article.getIdReadable().isBlank()) {
            return article.getIdReadable();
        }

        return article.getId();
    }
}