package io.spring.api;

import graphql.com.google.common.collect.ImmutableMap;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.UserQueryService;
import io.spring.application.data.UserData;
import io.spring.application.data.UserWithToken;
import io.spring.application.user.LoginParam;
import io.spring.application.user.RegisterParam;
import io.spring.application.user.UserService;
import io.spring.core.service.JwtService;
import io.spring.core.user.EncryptService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
public class UsersApi {
    private final UserRepository userRepository;
    private final UserQueryService userQueryService;
    private final EncryptService encryptService;
    private final JwtService jwtService;
    private final UserService userService;

    @PostMapping(value = "/users")
    public ResponseEntity<Map<String, UserWithToken>> createUser(@Valid @RequestBody RegisterParam registerParam) {
        User user = userService.createUser(registerParam);
        UserData userData = userQueryService.findById(user.getId()).orElseThrow(ResourceNotFoundException::new);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse(new UserWithToken(userData, jwtService.toToken(user))));
    }

    @PostMapping(value = "/users/login")
    public ResponseEntity<Map<String, UserWithToken>> userLogin(@Valid @RequestBody LoginParam loginParam) {
        Optional<User> optional = userRepository.findByEmail(loginParam.getEmail());
        if (optional.isPresent() && encryptService.check(loginParam.getPassword(), optional.get().getPassword())) {
            UserData userData = userQueryService.findById(optional.get().getId()).orElseThrow(ResourceNotFoundException::new);
            return ResponseEntity.ok(userResponse(new UserWithToken(userData, jwtService.toToken(optional.get()))));
        } else {
            throw new InvalidAuthenticationException();
        }
    }

    private Map<String, UserWithToken> userResponse(UserWithToken userWithToken) {
        return ImmutableMap.of("user", userWithToken);
    }
}
