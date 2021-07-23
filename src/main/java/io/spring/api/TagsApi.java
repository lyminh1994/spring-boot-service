package io.spring.api;

import graphql.com.google.common.collect.ImmutableMap;
import io.spring.application.TagsQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "tags")
public class TagsApi {
    private final TagsQueryService tagsQueryService;

    @GetMapping
    public ResponseEntity<Map<String, List<String>>> getTags() {
        return ResponseEntity.ok(ImmutableMap.of("tags", tagsQueryService.allTags()));
    }
}
