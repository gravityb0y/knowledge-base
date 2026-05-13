package ru.urfu.knowledge.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.urfu.knowledge.dto.YouTrackArticle;
import ru.urfu.knowledge.dto.YouTrackIssue;

import java.util.LinkedList;
import java.util.List;

@Service
public class YouTrackService {

    private final RestClient youTrackRestClient;

    @Autowired
    public YouTrackService(RestClient youTrackRestClient) {
        this.youTrackRestClient = youTrackRestClient;
    }

    public List<YouTrackIssue> getAllIssues(int pageSize) {
        List<YouTrackIssue> result = new LinkedList<>();
        int skip = 0;
        while (true) {
            List<YouTrackIssue> batch = fetchIssues(pageSize, skip);
            result.addAll(batch);
            if (batch.size() < pageSize) {
                break;
            }
            skip += pageSize;
        }

        return result;
    }

    public List<YouTrackArticle> getAllArticles(int pageSize) {
        List<YouTrackArticle> result = new LinkedList<>();

        int skip = 0;
        while (true) {
            List<YouTrackArticle> batch = fetchArticles(pageSize, skip);
            result.addAll(batch);

            if (batch.size() < pageSize) {
                break;
            }

            skip += pageSize;
        }

        return result;
    }

    private List<YouTrackIssue> fetchIssues(int top, int skip) {
        return youTrackRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/issues")
                        .queryParam("fields", "idReadable,summary,description")
                        .queryParam("$top", top)
                        .queryParam("$skip", skip)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    private List<YouTrackArticle> fetchArticles(int top, int skip) {
        return youTrackRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/articles")
                        .queryParam("fields", "id,idReadable,summary,content,updated")
                        .queryParam("$top", top)
                        .queryParam("$skip", skip)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

}
