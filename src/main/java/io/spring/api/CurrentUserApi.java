package io.spring.api;

import graphql.com.google.common.collect.ImmutableMap;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.UserQueryService;
import io.spring.application.data.UserData;
import io.spring.application.data.UserWithToken;
import io.spring.application.user.UpdateUserCommand;
import io.spring.application.user.UpdateUserParam;
import io.spring.application.user.UserService;
import io.spring.core.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/user")
public class CurrentUserApi {
    private final UserQueryService userQueryService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<Map<String, UserWithToken>> currentUser(@AuthenticationPrincipal User currentUser,
                                                           @RequestHeader(value = "Authorization") String authorization) {
        UserData userData = userQueryService.findById(currentUser.getId()).orElseThrow(ResourceNotFoundException::new);
        return ResponseEntity.ok(userResponse(new UserWithToken(userData, authorization.split(" ")[1])));
    }

    @PutMapping
    public ResponseEntity<Map<String, UserWithToken>> updateProfile(@AuthenticationPrincipal User currentUser,
                                                             @RequestHeader("Authorization") String token,
                                                             @Valid @RequestBody UpdateUserParam updateUserParam) {
        userService.updateUser(new UpdateUserCommand(currentUser, updateUserParam));
        UserData userData = userQueryService.findById(currentUser.getId()).orElseThrow(ResourceNotFoundException::new);
        return ResponseEntity.ok(userResponse(new UserWithToken(userData, token.split(" ")[1])));
    }

    private Map<String, UserWithToken> userResponse(UserWithToken userWithToken) {
        return ImmutableMap.of("user", userWithToken);
    }
}
