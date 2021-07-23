package io.spring.api;

import graphql.com.google.common.collect.ImmutableMap;
import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleQueryService;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.article.UpdateArticleParam;
import io.spring.application.data.ArticleData;
import io.spring.core.article.ArticleRepository;
import io.spring.core.service.AuthorizationService;
import io.spring.core.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/articles/{slug}")
public class ArticleApi {
    private final ArticleQueryService articleQueryService;
    private final ArticleRepository articleRepository;
    private final ArticleCommandService articleCommandService;

    @GetMapping
    public ResponseEntity<Map<String, ArticleData>> article(@PathVariable("slug") String slug,
                                                       @AuthenticationPrincipal User user) {
        return articleQueryService.findBySlug(slug, user)
                .map(articleData -> ResponseEntity.ok(articleResponse(articleData))).orElseThrow(ResourceNotFoundException::new);
    }

    @PutMapping
    public ResponseEntity<Map<String, ArticleData>> updateArticle(@PathVariable("slug") String slug,
                                                             @AuthenticationPrincipal User user,
                                                             @Valid @RequestBody UpdateArticleParam updateArticleParam) {
        return articleRepository.findBySlug(slug).map(article -> {
            if (!AuthorizationService.canWriteArticle(user, article)) throw new NoAuthorizationException();
            articleCommandService.updateArticle(article, updateArticleParam);
            return ResponseEntity.ok(articleResponse(articleQueryService.findBySlug(slug, user).orElseThrow(ResourceNotFoundException::new)));
        }).orElseThrow(ResourceNotFoundException::new);
    }

    @DeleteMapping
    public ResponseEntity<Object> deleteArticle(@PathVariable("slug") String slug, @AuthenticationPrincipal User user) {
        return articleRepository.findBySlug(slug).map(article -> {
            if (!AuthorizationService.canWriteArticle(user, article)) throw new NoAuthorizationException();
            articleRepository.remove(article);
            return ResponseEntity.noContent().build();
        }).orElseThrow(ResourceNotFoundException::new);
    }

    private Map<String, ArticleData> articleResponse(ArticleData articleData) {
        return ImmutableMap.of("article", articleData);
    }
}
