package ru.urfu.knowledge.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IndexingStartResponse {
    private String jobId;
    private IndexingJobStatus status;
    private String message;
}