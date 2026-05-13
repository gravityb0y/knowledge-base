package ru.urfu.knowledge.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IndexingResult {
    private int documentChunks;
    private int youTrackTaskChunks;
    private int youTrackArticleChunks;
    private int totalChunks;
}
