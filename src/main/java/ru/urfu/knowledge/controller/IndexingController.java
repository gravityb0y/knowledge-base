package ru.urfu.knowledge.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.urfu.knowledge.dto.IndexingJobResponse;
import ru.urfu.knowledge.dto.IndexingJobStatus;
import ru.urfu.knowledge.dto.IndexingJobType;
import ru.urfu.knowledge.dto.IndexingStartResponse;
import ru.urfu.knowledge.service.indexing.IndexingService;
import ru.urfu.knowledge.service.indexing.IndexingJob;
import ru.urfu.knowledge.service.indexing.IndexingJobRegistry;

import java.util.List;

@RestController
@RequestMapping("/api/indexing")
public class IndexingController {

    private final IndexingService indexingService;
    private final IndexingJobRegistry jobRegistry;

    public IndexingController(
            IndexingService indexingService,
            IndexingJobRegistry jobRegistry
    ) {
        this.indexingService = indexingService;
        this.jobRegistry = jobRegistry;
    }

    @PostMapping("/all")
    public ResponseEntity<IndexingStartResponse> reindexAll() {
        return start(IndexingJobType.ALL);
    }

    @PostMapping("/documents")
    public ResponseEntity<IndexingStartResponse> reindexDocuments() {
        return start(IndexingJobType.DOCUMENTS);
    }

    @PostMapping("/youtrack")
    public ResponseEntity<IndexingStartResponse> reindexYouTrack() {
        return start(IndexingJobType.YOUTRACK);
    }

    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<IndexingJobResponse> getJobStatus(@PathVariable String jobId) {
        return jobRegistry.findById(jobId)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/jobs")
    public ResponseEntity<List<IndexingJobResponse>> getJobs() {
        List<IndexingJobResponse> jobs = jobRegistry.findAll()
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(jobs);
    }

    private ResponseEntity<IndexingStartResponse> start(IndexingJobType type) {
        try {
            IndexingJob job = indexingService.startIndexing(type);

            return ResponseEntity.ok(
                    IndexingStartResponse.builder()
                            .jobId(job.getJobId())
                            .status(job.getStatus())
                            .message("Синхронизация началась")
                            .build()
            );
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(409).body(
                    IndexingStartResponse.builder()
                            .status(IndexingJobStatus.RUNNING)
                            .message(exception.getMessage())
                            .build()
            );
        }
    }

    private IndexingJobResponse toResponse(IndexingJob job) {
        return IndexingJobResponse.builder()
                .jobId(job.getJobId())
                .type(job.getType())
                .status(job.getStatus())
                .createdAt(job.getCreatedAt())
                .startedAt(job.getStartedAt())
                .finishedAt(job.getFinishedAt())
                .result(job.getResult())
                .errorMessage(job.getErrorMessage())
                .build();
    }
}