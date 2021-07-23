package io.spring.core.user;

import io.spring.StringUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class User {
    private String id;
    private String email;
    private String username;
    private String password;
    private String bio;
    private String image;

    public User(String email, String username, String password, String bio, String image) {
        this.id = UUID.randomUUID().toString();
        this.email = email;
        this.username = username;
        this.password = password;
        this.bio = bio;
        this.image = image;
    }

    public void update(String email, String username, String password, String bio, String image) {
        if (StringUtil.isNotEmpty(email)) {
            this.email = email;
        }

        if (StringUtil.isNotEmpty(username)) {
            this.username = username;
        }

        if (StringUtil.isNotEmpty(password)) {
            this.password = password;
        }

        if (StringUtil.isNotEmpty(bio)) {
            this.bio = bio;
        }

        if (StringUtil.isNotEmpty(image)) {
            this.image = image;
        }
    }
}
