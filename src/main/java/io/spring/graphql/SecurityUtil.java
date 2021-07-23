package io.spring.graphql;

import io.spring.core.user.User;
import lombok.experimental.UtilityClass;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@UtilityClass
public class SecurityUtil {

    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken || authentication.getPrincipal() == null) {
            return Optional.empty();
        }
        User currentUser = (User) authentication.getPrincipal();
        return Optional.of(currentUser);
    }

}
