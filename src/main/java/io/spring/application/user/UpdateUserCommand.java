package io.spring.application.user;

import io.spring.core.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@UpdateUserConstraint
public class UpdateUserCommand {

    private final User targetUser;
    private final UpdateUserParam param;
}
