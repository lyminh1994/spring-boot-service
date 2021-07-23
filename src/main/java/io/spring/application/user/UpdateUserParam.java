package io.spring.application.user;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;

@Getter
@JsonRootName("user")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserParam {

    @Builder.Default
    @Email(message = "should be an email")
    private final String email = "";

    @Builder.Default
    private final String password = "";
    @Builder.Default
    private final String username = "";
    @Builder.Default
    private final String bio = "";
    @Builder.Default
    private final String image = "";
}
