package ru.urfu.knowledge.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.urfu.knowledge.service.WeaviateService;

@RestController
@RequestMapping("/ask")
public class QuestionController {

    private final WeaviateService weaviateService;

    @Autowired
    public QuestionController(WeaviateService weaviateService) {
        this.weaviateService = weaviateService;
    }

    @GetMapping
    public String getAnswer(@RequestParam String q) {

        return weaviateService.generateAnswer(q);
    }
}
