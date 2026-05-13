package ru.urfu.knowledge.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DocumentUploadResponse {
    private String sourceId;
    private String fileName;
    private String title;
    private String sourceType;
    private int chunksCount;
    private String message;
}