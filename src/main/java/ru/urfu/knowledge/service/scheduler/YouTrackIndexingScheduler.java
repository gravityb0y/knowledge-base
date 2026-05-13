package ru.urfu.knowledge.service.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.urfu.knowledge.dto.IndexingJobType;
import ru.urfu.knowledge.service.indexing.IndexingService;

@Component
public class YouTrackIndexingScheduler {

    private static final Logger log = LoggerFactory.getLogger(YouTrackIndexingScheduler.class);

    private final IndexingService indexingService;

    public YouTrackIndexingScheduler(IndexingService indexingService) {
        this.indexingService = indexingService;
    }

    @Scheduled(cron = "${indexing.youtrack.cron:0 0 3 * * *}")
    public void reindexYouTrackDaily() {
        try {
            indexingService.startIndexing(IndexingJobType.YOUTRACK);
            log.info("Ежедневная синхронизация YouTrack поставлена в очередь");
        } catch (IllegalStateException exception) {
            log.warn("Ежедневная синхронизация YouTrack пропущена: {}", exception.getMessage());
        }
    }
}