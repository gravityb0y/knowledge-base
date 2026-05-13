package ru.urfu.knowledge.service.indexing;

import org.springframework.stereotype.Service;
import ru.urfu.knowledge.dto.IndexingJobType;

@Service
public class IndexingService {

    private final IndexingJobRegistry jobRegistry;
    private final IndexingAsyncWorker indexingAsyncWorker;

    public IndexingService(
            IndexingJobRegistry jobRegistry,
            IndexingAsyncWorker indexingAsyncWorker
    ) {
        this.jobRegistry = jobRegistry;
        this.indexingAsyncWorker = indexingAsyncWorker;
    }

    public IndexingJob startIndexing(IndexingJobType type) {
        if (jobRegistry.hasRunningJob()) {
            throw new IllegalStateException("Индексация уже выполняется");
        }

        IndexingJob job = jobRegistry.createJob(type);
        indexingAsyncWorker.run(job.getJobId());

        return job;
    }
}