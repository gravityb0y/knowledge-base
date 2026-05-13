package ru.urfu.knowledge.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class QuestionResponse {
    private String answer;
    private List<SourceDto> sources;
}
