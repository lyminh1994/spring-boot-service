package io.spring.core.article;

import io.spring.StringUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class Article {
    private String userId;
    private String id;
    private String slug;
    private String title;
    private String description;
    private String body;
    private List<Tag> tags;
    private DateTime createdAt;
    private DateTime updatedAt;

    public Article(String title, String description, String body, List<String> tagList, String userId) {
        this(title, description, body, tagList, userId, new DateTime());
    }

    public Article(String title, String description, String body, List<String> tagList, String userId, DateTime createdAt) {
        this.id = UUID.randomUUID().toString();
        this.slug = toSlug(title);
        this.title = title;
        this.description = description;
        this.body = body;
        this.tags = new HashSet<>(tagList).stream().map(Tag::new).collect(toList());
        this.userId = userId;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public static String toSlug(String title) {
        return title.toLowerCase().replaceAll("[\\&|[\\uFE30-\\uFFA0]|\\’|\\”|\\s\\?\\,\\.]+", "-");
    }

    public void update(String title, String description, String body) {
        if (StringUtil.isNotEmpty(title)) {
            this.title = title;
            this.slug = toSlug(title);
            this.updatedAt = new DateTime();
        }
        if (StringUtil.isNotEmpty(description)) {
            this.description = description;
            this.updatedAt = new DateTime();
        }
        if (StringUtil.isNotEmpty(body)) {
            this.body = body;
            this.updatedAt = new DateTime();
        }
    }
}
