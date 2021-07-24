package io.spring.application.article;

import io.spring.application.*;
import io.spring.application.CursorPager.Direction;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleDataList;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.repository.MyBatisArticleFavoriteRepository;
import io.spring.infrastructure.repository.MyBatisArticleRepository;
import io.spring.infrastructure.repository.MyBatisUserRepository;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.*;

@Import({
        ArticleQueryService.class,
        MyBatisUserRepository.class,
        MyBatisArticleRepository.class,
        MyBatisArticleFavoriteRepository.class
})
public class ArticleQueryServiceTest extends DbTestBase {

    @Autowired
    private ArticleQueryService queryService;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArticleFavoriteRepository articleFavoriteRepository;

    private User user;

    private Article article;

    @Before
    public void setUp() {
        user = new User("aisensiy@gmail.com", "aisensiy", "123", "", "");
        userRepository.save(user);
        article = new Article("test", "desc", "body", Arrays.asList("java", "spring"), user.getId(), new DateTime());
        articleRepository.save(article);
    }

    @Test
    public void should_fetch_article_success() {
        Optional<ArticleData> optional = queryService.findById(article.getId(), user);
        assertTrue(optional.isPresent());

        ArticleData fetched = optional.get();
        assertEquals(0, fetched.getFavoritesCount());
        assertFalse(fetched.isFavorited());
        assertNotNull(fetched.getCreatedAt());
        assertNotNull(fetched.getUpdatedAt());
        assertTrue(fetched.getTagList().contains("java"));
    }

    @Test
    public void should_get_article_with_right_favorite_and_favorite_count() {
        User anotherUser = new User("other@test.com", "other", "123", "", "");
        userRepository.save(anotherUser);
        articleFavoriteRepository.save(new ArticleFavorite(article.getId(), anotherUser.getId()));

        Optional<ArticleData> optional = queryService.findById(article.getId(), anotherUser);
        assertTrue(optional.isPresent());

        ArticleData articleData = optional.get();
        assertEquals(1, articleData.getFavoritesCount());
        assertTrue(articleData.isFavorited());
    }

    @Test
    public void should_get_default_article_list() {
        Article anotherArticle = new Article("new article", "desc", "body", Collections.singletonList("test"), user.getId(), new DateTime().minusHours(1));
        articleRepository.save(anotherArticle);

        ArticleDataList recentArticles = queryService.findRecentArticles(null, null, null, new Page(), user);
        assertEquals(2, recentArticles.getCount());
        assertEquals(2, recentArticles.getArticleDatas().size());
        assertEquals(recentArticles.getArticleDatas().get(0).getId(), article.getId());

        ArticleDataList nodata = queryService.findRecentArticles(null, null, null, new Page(2, 10), user);
        assertEquals(2, nodata.getCount());
        assertEquals(0, nodata.getArticleDatas().size());
    }

    @Test
    public void should_get_default_article_list_by_cursor() {
        Article anotherArticle = new Article("new article", "desc", "body", Collections.singletonList("test"), user.getId(), new DateTime().minusHours(1));
        articleRepository.save(anotherArticle);

        CursorPager<ArticleData> recentArticles = queryService.findRecentArticlesWithCursor(null, null, null, new CursorPageParameter<>(null, 20, Direction.NEXT), user);
        assertEquals(2, recentArticles.getData().size());
        assertEquals(recentArticles.getData().get(0).getId(), article.getId());

        CursorPager<ArticleData> nodata = queryService.findRecentArticlesWithCursor(null, null, null, new CursorPageParameter<>(DateTimeCursor.parse(recentArticles.getEndCursor().toString()), 20, Direction.NEXT), user);
        assertEquals(0, nodata.getData().size());
        assertNull(nodata.getStartCursor());

        CursorPager<ArticleData> prevArticles = queryService.findRecentArticlesWithCursor(null, null, null, new CursorPageParameter<>(null, 20, Direction.PREV), user);
        assertEquals(2, prevArticles.getData().size());
    }

    @Test
    public void should_query_article_by_author() {
        User anotherUser = new User("other@email.com", "other", "123", "", "");
        userRepository.save(anotherUser);

        Article anotherArticle = new Article("new article", "desc", "body", Collections.singletonList("test"), anotherUser.getId());
        articleRepository.save(anotherArticle);

        ArticleDataList recentArticles = queryService.findRecentArticles(null, user.getUsername(), null, new Page(), user);
        assertEquals(1, recentArticles.getArticleDatas().size());
        assertEquals(1, recentArticles.getCount());
    }

    @Test
    public void should_query_article_by_favorite() {
        User anotherUser = new User("other@email.com", "other", "123", "", "");
        userRepository.save(anotherUser);

        Article anotherArticle = new Article("new article", "desc", "body", Collections.singletonList("test"), anotherUser.getId());
        articleRepository.save(anotherArticle);

        ArticleFavorite articleFavorite = new ArticleFavorite(article.getId(), anotherUser.getId());
        articleFavoriteRepository.save(articleFavorite);

        ArticleDataList recentArticles = queryService.findRecentArticles(null, null, anotherUser.getUsername(), new Page(), anotherUser);
        assertEquals(1, recentArticles.getArticleDatas().size());
        assertEquals(1, recentArticles.getCount());
        ArticleData articleData = recentArticles.getArticleDatas().get(0);
        assertEquals(articleData.getId(), article.getId());
        assertEquals(1, articleData.getFavoritesCount());
        assertTrue(articleData.isFavorited());
    }

    @Test
    public void should_query_article_by_tag() {
        Article anotherArticle = new Article("new article", "desc", "body", Collections.singletonList("test"), user.getId());
        articleRepository.save(anotherArticle);

        ArticleDataList recentArticles = queryService.findRecentArticles("spring", null, null, new Page(), user);
        assertEquals(1, recentArticles.getArticleDatas().size());
        assertEquals(1, recentArticles.getCount());
        assertEquals(recentArticles.getArticleDatas().get(0).getId(), article.getId());

        ArticleDataList noTag = queryService.findRecentArticles("notag", null, null, new Page(), user);
        assertEquals(0, noTag.getCount());
    }

    @Test
    public void should_show_following_if_user_followed_author() {
        User anotherUser = new User("other@email.com", "other", "123", "", "");
        userRepository.save(anotherUser);

        FollowRelation followRelation = new FollowRelation(anotherUser.getId(), user.getId());
        userRepository.saveRelation(followRelation);

        ArticleDataList recentArticles = queryService.findRecentArticles(null, null, null, new Page(), anotherUser);
        assertEquals(1, recentArticles.getCount());
        ArticleData articleData = recentArticles.getArticleDatas().get(0);
        assertTrue(articleData.getProfileData().isFollowing());
    }

    @Test
    public void should_get_user_feed() {
        User anotherUser = new User("other@email.com", "other", "123", "", "");
        userRepository.save(anotherUser);

        FollowRelation followRelation = new FollowRelation(anotherUser.getId(), user.getId());
        userRepository.saveRelation(followRelation);

        ArticleDataList userFeed = queryService.findUserFeed(user, new Page());
        assertEquals(0, userFeed.getCount());

        ArticleDataList anotherUserFeed = queryService.findUserFeed(anotherUser, new Page());
        assertEquals(1, anotherUserFeed.getCount());
        ArticleData articleData = anotherUserFeed.getArticleDatas().get(0);
        assertTrue(articleData.getProfileData().isFollowing());
    }
}
