package ru.urfu.knowledge.service.indexing;

import org.springframework.stereotype.Component;
import ru.urfu.knowledge.dto.IndexingJobStatus;
import ru.urfu.knowledge.dto.IndexingJobType;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class IndexingJobRegistry {

    private final ConcurrentHashMap<String, IndexingJob> jobs = new ConcurrentHashMap<>();

    public IndexingJob createJob(IndexingJobType type) {
        IndexingJob job = IndexingJob.builder()
                .jobId(UUID.randomUUID().toString())
                .type(type)
                .status(IndexingJobStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        jobs.put(job.getJobId(), job);
        return job;
    }

    public Optional<IndexingJob> findById(String jobId) {
        return Optional.ofNullable(jobs.get(jobId));
    }

    public Collection<IndexingJob> findAll() {
        return jobs.values()
                .stream()
                .sorted(Comparator.comparing(IndexingJob::getCreatedAt).reversed())
                .toList();
    }

    public boolean hasRunningJob() {
        return jobs.values()
                .stream()
                .anyMatch(job ->
                        job.getStatus() == IndexingJobStatus.PENDING
                                || job.getStatus() == IndexingJobStatus.RUNNING
                );
    }
}