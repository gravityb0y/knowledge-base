package ru.urfu.knowledge.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.urfu.knowledge.dto.QuestionRequest;
import ru.urfu.knowledge.dto.QuestionResponse;
import ru.urfu.knowledge.service.WeaviateService;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final WeaviateService weaviateService;

    @Autowired
    public QuestionController(WeaviateService weaviateService) {
        this.weaviateService = weaviateService;
    }

    @PostMapping
    public ResponseEntity<QuestionResponse> askQuestion(@RequestBody QuestionRequest request) {
        if (request.getQuestion() == null || request.getQuestion().isBlank()) {
            return ResponseEntity.badRequest().body(
                    QuestionResponse.builder()
                            .answer("Вопрос не должен быть пустым")
                            .sources(java.util.List.of())
                            .build()
            );
        }

        return ResponseEntity.ok(weaviateService.generateAnswer(request.getQuestion()));
    }
}
