package ru.urfu.knowledge.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SourceDto {
    private String title;
    private String sourceType;
    private String sourceName;
    private String url;
    private String fragment;
}
