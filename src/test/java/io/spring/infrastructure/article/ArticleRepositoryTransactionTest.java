package io.spring.infrastructure.article;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.mybatis.mapper.ArticleMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

import static org.junit.Assert.assertNull;

@ActiveProfiles(value = "test")
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:db/create_tables.sql")
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@RunWith(SpringRunner.class)
public class ArticleRepositoryTransactionTest {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArticleMapper articleMapper;

    @Test
    public void transactional_test() {
        User user = new User("aisensiy@gmail.com", "aisensiy", "123", "bio", "default");
        userRepository.save(user);
        Article article = new Article("test", "desc", "body", Arrays.asList("java", "spring"), user.getId());
        articleRepository.save(article);
        Article anotherArticle = new Article("test", "desc", "body", Arrays.asList("java", "spring", "other"), user.getId());
        try {
            articleRepository.save(anotherArticle);
        } catch (Exception e) {
            assertNull(articleMapper.findTag("other"));
        }
    }
}
