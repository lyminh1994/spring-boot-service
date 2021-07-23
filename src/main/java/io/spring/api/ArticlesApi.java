package io.spring.api;

import graphql.com.google.common.collect.ImmutableMap;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleQueryService;
import io.spring.application.Page;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.article.NewArticleParam;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleDataList;
import io.spring.core.article.Article;
import io.spring.core.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/articles")
public class ArticlesApi {
    private final ArticleCommandService articleCommandService;
    private final ArticleQueryService articleQueryService;

    @PostMapping
    public ResponseEntity<Map<String, ArticleData>> createArticle(@Valid @RequestBody NewArticleParam newArticleParam,
                                                                  @AuthenticationPrincipal User user) {
        Article article = articleCommandService.createArticle(newArticleParam, user);
        return ResponseEntity.ok(ImmutableMap.of("article", articleQueryService.findById(article.getId(), user).orElseThrow(ResourceNotFoundException::new)));
    }

    @GetMapping(path = "feed")
    public ResponseEntity<ArticleDataList> getFeed(@RequestParam(value = "offset", defaultValue = "0") int offset,
                                                   @RequestParam(value = "limit", defaultValue = "20") int limit,
                                                   @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(articleQueryService.findUserFeed(user, new Page(offset, limit)));
    }

    @GetMapping
    public ResponseEntity<ArticleDataList> getArticles(@RequestParam(value = "offset", defaultValue = "0") int offset,
                                                       @RequestParam(value = "limit", defaultValue = "20") int limit,
                                                       @RequestParam(value = "tag", required = false) String tag,
                                                       @RequestParam(value = "favorited", required = false) String favoriteBy,
                                                       @RequestParam(value = "author", required = false) String author,
                                                       @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(articleQueryService.findRecentArticles(tag, author, favoriteBy, new Page(offset, limit), user));
    }
}
