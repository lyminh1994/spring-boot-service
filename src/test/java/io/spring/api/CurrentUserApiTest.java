package io.spring.api;

import graphql.com.google.common.collect.ImmutableMap;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.UserQueryService;
import io.spring.application.user.UserService;
import io.spring.core.user.User;
import io.spring.infrastructure.service.NaiveEncryptService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebMvcTest(CurrentUserApi.class)
@Import({
        WebSecurityConfig.class,
        JacksonCustomizations.class,
        UserService.class,
        ValidationAutoConfiguration.class,
        NaiveEncryptService.class
})
public class CurrentUserApiTest extends TestWithCurrentUser {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserQueryService userQueryService;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        RestAssuredMockMvc.mockMvc(mvc);
    }

    @Test
    public void should_get_current_user_with_token() {
        when(userQueryService.findById(any())).thenReturn(Optional.of(userData));

        given()
                .header("Authorization", "Token " + token)
                .contentType("application/json")
                .when()
                .get("/user")
                .then()
                .statusCode(200)
                .body("user.email", equalTo(email))
                .body("user.username", equalTo(username))
                .body("user.bio", equalTo(""))
                .body("user.image", equalTo(defaultAvatar))
                .body("user.token", equalTo(token));
    }

    @Test
    public void should_get_401_without_token() {
        given().contentType("application/json").when().get("/user").then().statusCode(401);
    }

    @Test
    public void should_get_401_with_invalid_token() {
        String invalidToken = "asdfasd";
        when(jwtService.getSubFromToken(invalidToken)).thenReturn(Optional.empty());
        given()
                .contentType("application/json")
                .header("Authorization", "Token " + invalidToken)
                .when()
                .get("/user")
                .then()
                .statusCode(401);
    }

    @Test
    public void should_update_current_user_profile() {
        String newEmail = "newemail@example.com";
        String newBio = "updated";
        String newUsername = "newusernamee";

        Map<String, Object> param = ImmutableMap.of("user", ImmutableMap.of("email", newEmail, "bio", newBio, "username", newUsername));

        when(userRepository.findByUsername(newUsername)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(newEmail)).thenReturn(Optional.empty());
        when(userQueryService.findById(user.getId())).thenReturn(Optional.of(userData));

        given()
                .contentType("application/json")
                .header("Authorization", "Token " + token)
                .body(param)
                .when()
                .put("/user")
                .then()
                .statusCode(200);
    }

    @Test
    public void should_get_error_if_email_exists_when_update_user_profile() {
        String newEmail = "newemail@example.com";
        String newBio = "updated";
        String newUsername = "newusernamee";

        Map<String, Object> param = prepareUpdateParam(newEmail, newBio, newUsername);

        when(userRepository.findByEmail(newEmail)).thenReturn(Optional.of(new User(newEmail, "username", "123", "", "")));
        when(userRepository.findByUsername(newUsername)).thenReturn(Optional.empty());
        when(userQueryService.findById(user.getId())).thenReturn(Optional.of(userData));

        given()
                .contentType("application/json")
                .header("Authorization", "Token " + token)
                .body(param)
                .when()
                .put("/user")
                .prettyPeek()
                .then()
                .statusCode(422)
                .body("errors.email[0]", equalTo("email already exist"));
    }

    @Test
    public void should_get_401_if_not_login() {
        given()
                .contentType("application/json")
                .body(ImmutableMap.of("user", new HashMap<String, Object>()))
                .when()
                .put("/user")
                .then()
                .statusCode(401);
    }

    private Map<String, Object> prepareUpdateParam(final String newEmail, final String newBio, final String newUsername) {
        return ImmutableMap.of("user", ImmutableMap.of("email", newEmail, "bio", newBio, "username", newUsername));
    }
}
