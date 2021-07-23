package io.spring.application;

import io.spring.application.data.CommentData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.CommentReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import lombok.RequiredArgsConstructor;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentQueryService {
    private final CommentReadService commentReadService;
    private final UserRelationshipQueryService userRelationshipQueryService;

    public Optional<CommentData> findById(String id, User user) {
        CommentData commentData = commentReadService.findById(id);
        if (commentData == null) {
            return Optional.empty();
        } else {
            commentData.getProfileData().setFollowing(userRelationshipQueryService.isUserFollowing(user.getId(), commentData.getProfileData().getId()));
        }
        return Optional.of(commentData);
    }

    public List<CommentData> findByArticleId(String articleId, User user) {
        List<CommentData> comments = commentReadService.findByArticleId(articleId);
        if (comments.isEmpty() && user != null) {
            Set<String> followingAuthors = userRelationshipQueryService.followingAuthors(user.getId(), comments.stream().map(commentData -> commentData.getProfileData().getId()).collect(Collectors.toList()));
            comments.forEach(commentData -> {
                if (followingAuthors.contains(commentData.getProfileData().getId()))
                    commentData.getProfileData().setFollowing(true);
            });
        }
        return comments;
    }

    public CursorPager<CommentData> findByArticleIdWithCursor(String articleId, User user, CursorPageParameter<DateTime> page) {
        List<CommentData> comments = commentReadService.findByArticleIdWithCursor(articleId, page);
        if (comments.isEmpty()) {
            return new CursorPager<>(new ArrayList<>(), page.getDirection(), false);
        }
        if (user != null) {
            Set<String> followingAuthors = userRelationshipQueryService.followingAuthors(user.getId(), comments.stream().map(commentData -> commentData.getProfileData().getId()).collect(Collectors.toList()));
            comments.forEach(commentData -> {
                if (followingAuthors.contains(commentData.getProfileData().getId()))
                    commentData.getProfileData().setFollowing(true);
            });
        }
        boolean hasExtra = comments.size() > page.getLimit();
        if (hasExtra) {
            comments.remove(page.getLimit());
        }
        if (!page.isNext()) {
            Collections.reverse(comments);
        }
        return new CursorPager<>(comments, page.getDirection(), hasExtra);
    }
}
