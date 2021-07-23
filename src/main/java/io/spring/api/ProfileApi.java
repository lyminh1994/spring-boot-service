package io.spring.api;

import graphql.com.google.common.collect.ImmutableMap;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ProfileData;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "profiles/{username}")
public class ProfileApi {
    private final ProfileQueryService profileQueryService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Map<String, ProfileData>> getProfile(@PathVariable("username") String username,
                                                          @AuthenticationPrincipal User user) {
        return profileQueryService.findByUsername(username, user).map(this::profileResponse).orElseThrow(ResourceNotFoundException::new);
    }

    @PostMapping(path = "follow")
    public ResponseEntity<Map<String, ProfileData>> follow(@PathVariable("username") String username,
                                                      @AuthenticationPrincipal User user) {
        return userRepository.findByUsername(username).map(target -> {
            FollowRelation followRelation = new FollowRelation(user.getId(), target.getId());
            userRepository.saveRelation(followRelation);
            return profileResponse(profileQueryService.findByUsername(username, user).orElseThrow(ResourceNotFoundException::new));
        }).orElseThrow(ResourceNotFoundException::new);
    }

    @DeleteMapping(path = "follow")
    public ResponseEntity<Map<String, ProfileData>> unFollow(@PathVariable("username") String username,
                                                        @AuthenticationPrincipal User user) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User target = userOptional.get();
            return userRepository.findRelation(user.getId(), target.getId()).map(relation -> {
                userRepository.removeRelation(relation);
                return profileResponse(profileQueryService.findByUsername(username, user).orElseThrow(ResourceNotFoundException::new));
            }).orElseThrow(ResourceNotFoundException::new);
        } else {
            throw new ResourceNotFoundException();
        }
    }

    private ResponseEntity<Map<String, ProfileData>> profileResponse(ProfileData profile) {
        return ResponseEntity.ok(ImmutableMap.of("profile", profile));
    }
}
