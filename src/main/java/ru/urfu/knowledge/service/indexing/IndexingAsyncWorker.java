package ru.urfu.knowledge.service.indexing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.urfu.knowledge.dto.IndexingJobStatus;
import ru.urfu.knowledge.dto.IndexingResult;
import ru.urfu.knowledge.dto.KnowledgeChunk;
import ru.urfu.knowledge.service.processor.FileProcessorService;
import ru.urfu.knowledge.service.processor.YouTrackArticleProcessorService;
import ru.urfu.knowledge.service.processor.YouTrackIssueProcessorService;
import ru.urfu.knowledge.service.WeaviateService;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class IndexingAsyncWorker {

    private static final Logger log = LoggerFactory.getLogger(IndexingAsyncWorker.class);

    private final FileProcessorService fileProcessorService;
    private final YouTrackIssueProcessorService youTrackProcessorService;
    private final YouTrackArticleProcessorService youTrackArticleProcessorService;
    private final WeaviateService weaviateService;
    private final IndexingJobRegistry jobRegistry;

    public IndexingAsyncWorker(
            FileProcessorService fileProcessorService,
            YouTrackIssueProcessorService youTrackProcessorService,
            YouTrackArticleProcessorService youTrackArticleProcessorService,
            WeaviateService weaviateService,
            IndexingJobRegistry jobRegistry
    ) {
        this.fileProcessorService = fileProcessorService;
        this.youTrackProcessorService = youTrackProcessorService;
        this.youTrackArticleProcessorService = youTrackArticleProcessorService;
        this.weaviateService = weaviateService;
        this.jobRegistry = jobRegistry;
    }

    @Async("indexingTaskExecutor")
    public void run(String jobId) {
        IndexingJob job = jobRegistry.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Задача индексации не найдена: " + jobId));

        job.setStatus(IndexingJobStatus.RUNNING);
        job.setStartedAt(LocalDateTime.now());

        try {
            IndexingResult result = switch (job.getType()) {
                case ALL -> reindexAllInternal();
                case DOCUMENTS -> reindexDocumentsInternal();
                case YOUTRACK -> reindexYouTrackInternal();
            };

            job.setResult(result);
            job.setStatus(IndexingJobStatus.COMPLETED);
            job.setFinishedAt(LocalDateTime.now());
        } catch (Exception exception) {
            job.setStatus(IndexingJobStatus.FAILED);
            job.setErrorMessage(exception.getMessage());
            job.setFinishedAt(LocalDateTime.now());

            log.error("Ошибка индексации. jobId={}, type={}", job.getJobId(), job.getType(), exception);
        }
    }

    private IndexingResult reindexAllInternal() {
        weaviateService.deleteDocuments();
        weaviateService.deleteYouTrack();

        List<KnowledgeChunk> documentChunks = fileProcessorService.process();
        List<KnowledgeChunk> taskChunks = youTrackProcessorService.process();
        List<KnowledgeChunk> articleChunks = youTrackArticleProcessorService.process();

        save(documentChunks);
        save(taskChunks);
        save(articleChunks);

        return buildResult(documentChunks, taskChunks, articleChunks);
    }

    private IndexingResult reindexDocumentsInternal() {
        weaviateService.deleteDocuments();

        List<KnowledgeChunk> documentChunks = fileProcessorService.process();
        save(documentChunks);

        return IndexingResult.builder()
                .documentChunks(documentChunks.size())
                .youTrackTaskChunks(0)
                .youTrackArticleChunks(0)
                .totalChunks(documentChunks.size())
                .build();
    }

    private IndexingResult reindexYouTrackInternal() {
        weaviateService.deleteYouTrack();

        List<KnowledgeChunk> taskChunks = youTrackProcessorService.process();
        List<KnowledgeChunk> articleChunks = youTrackArticleProcessorService.process();

        save(taskChunks);
        save(articleChunks);

        return IndexingResult.builder()
                .documentChunks(0)
                .youTrackTaskChunks(taskChunks.size())
                .youTrackArticleChunks(articleChunks.size())
                .totalChunks(taskChunks.size() + articleChunks.size())
                .build();
    }

    private void save(List<KnowledgeChunk> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            log.info("Нет чанков для сохранения");
            return;
        }

        weaviateService.saveChunks(chunks);
    }

    private IndexingResult buildResult(
            List<KnowledgeChunk> documentChunks,
            List<KnowledgeChunk> taskChunks,
            List<KnowledgeChunk> articleChunks
    ) {
        int documentCount = documentChunks.size();
        int taskCount = taskChunks.size();
        int articleCount = articleChunks.size();

        return IndexingResult.builder()
                .documentChunks(documentCount)
                .youTrackTaskChunks(taskCount)
                .youTrackArticleChunks(articleCount)
                .totalChunks(documentCount + taskCount + articleCount)
                .build();
    }
}