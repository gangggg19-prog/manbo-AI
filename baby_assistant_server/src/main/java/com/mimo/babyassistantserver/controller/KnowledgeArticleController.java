package com.mimo.babyassistantserver.controller;

import java.util.List;
import com.mimo.babyassistantserver.dto.knowledge.KnowledgeArticleResponse;
import com.mimo.babyassistantserver.service.KnowledgeArticleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Public, read-only resource centre endpoints. */
@RestController
@RequestMapping("/api/v1/knowledge-articles")
public class KnowledgeArticleController {
    private final KnowledgeArticleService service;
    public KnowledgeArticleController(KnowledgeArticleService service) { this.service = service; }
    @GetMapping public List<KnowledgeArticleResponse> list(@RequestParam(required = false) Integer ageMonths, @RequestParam(required = false) String category) { return service.list(ageMonths, category); }
    @GetMapping("/{slug}") public KnowledgeArticleResponse detail(@PathVariable String slug) { return service.getBySlug(slug); }
}