package ru.urfu.knowledge.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class YouTrackArticle {
    private String id;
    private String idReadable;
    private String summary;
    private String content;
    private Long updated;
}
