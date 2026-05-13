package ru.urfu.knowledge.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class IndexingJobResponse {
    private String jobId;
    private IndexingJobType type;
    private IndexingJobStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private IndexingResult result;
    private String errorMessage;
}