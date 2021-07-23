package io.spring.api;

import graphql.com.google.common.collect.ImmutableMap;
import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.CommentQueryService;
import io.spring.application.comment.NewCommentParam;
import io.spring.application.data.CommentData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.core.service.AuthorizationService;
import io.spring.core.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/articles/{slug}/comments")
public class CommentsApi {
    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;
    private final CommentQueryService commentQueryService;

    @PostMapping
    public ResponseEntity<Map<String, CommentData>> createComment(@PathVariable("slug") String slug,
                                                                  @AuthenticationPrincipal User user,
                                                                  @Valid @RequestBody NewCommentParam newCommentParam) {
        Article article = articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
        Comment comment = new Comment(newCommentParam.getBody(), user.getId(), article.getId());
        commentRepository.save(comment);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                commentResponse(commentQueryService.findById(comment.getId(), user).orElseThrow(ResourceNotFoundException::new)));
    }

    @GetMapping
    public ResponseEntity<Map<String, List<CommentData>>> getComments(@PathVariable("slug") String slug, @AuthenticationPrincipal User user) {
        Article article = articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
        List<CommentData> comments = commentQueryService.findByArticleId(article.getId(), user);
        return ResponseEntity.ok(ImmutableMap.of("comments", comments));
    }

    @DeleteMapping(value = "{id}")
    public ResponseEntity<Object> deleteComment(@PathVariable("slug") String slug, @PathVariable("id") String commentId,
                                                @AuthenticationPrincipal User user) {
        Article article = articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
        return commentRepository.findById(article.getId(), commentId).map(comment -> {
            if (!AuthorizationService.canWriteComment(user, article, comment)) throw new NoAuthorizationException();
            commentRepository.remove(comment);
            return ResponseEntity.noContent().build();
        }).orElseThrow(ResourceNotFoundException::new);
    }

    private Map<String, CommentData> commentResponse(CommentData commentData) {
        return ImmutableMap.of("comment", commentData);
    }
}
