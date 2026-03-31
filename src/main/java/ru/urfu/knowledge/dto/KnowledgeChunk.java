package ru.urfu.knowledge.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class KnowledgeChunk {

    private String content;

    private String sourceType;
    private String sourceId; // id документа в источнике
    private String sourceName;

    private String title;
    private String url;

    private Integer chunkIndex;

    private String sourceHash; // для возможности отслеживать изменения документа

}
