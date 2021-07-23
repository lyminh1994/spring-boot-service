package io.spring.api;

import graphql.com.google.common.collect.ImmutableMap;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleQueryService;
import io.spring.application.data.ArticleData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.core.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "articles/{slug}/favorite")
public class ArticleFavoriteApi {
    private final ArticleFavoriteRepository articleFavoriteRepository;
    private final ArticleRepository articleRepository;
    private final ArticleQueryService articleQueryService;

    @PostMapping
    public ResponseEntity<Map<String, ArticleData>> favoriteArticle(@PathVariable("slug") String slug, @AuthenticationPrincipal User user) {
        Article article = articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
        ArticleFavorite articleFavorite = new ArticleFavorite(article.getId(), user.getId());
        articleFavoriteRepository.save(articleFavorite);
        return responseArticleData(articleQueryService.findBySlug(slug, user).orElseThrow(ResourceNotFoundException::new));
    }

    @DeleteMapping
    public ResponseEntity<Map<String, ArticleData>> unFavoriteArticle(@PathVariable("slug") String slug, @AuthenticationPrincipal User user) {
        Article article = articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
        articleFavoriteRepository.find(article.getId(), user.getId()).ifPresent(articleFavoriteRepository::remove);
        return responseArticleData(articleQueryService.findBySlug(slug, user).orElseThrow(ResourceNotFoundException::new));
    }

    private ResponseEntity<Map<String, ArticleData>> responseArticleData(final ArticleData articleData) {
        return ResponseEntity.ok(ImmutableMap.of("article", articleData));
    }
}
