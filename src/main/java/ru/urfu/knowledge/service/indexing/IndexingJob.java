package ru.urfu.knowledge.service.indexing;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.urfu.knowledge.dto.IndexingJobStatus;
import ru.urfu.knowledge.dto.IndexingJobType;
import ru.urfu.knowledge.dto.IndexingResult;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class IndexingJob {
    private String jobId;
    private IndexingJobType type;
    private IndexingJobStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private IndexingResult result;
    private String errorMessage;
}