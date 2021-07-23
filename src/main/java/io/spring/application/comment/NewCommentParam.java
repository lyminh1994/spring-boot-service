package io.spring.application.comment;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonRootName("comment")
public class NewCommentParam {

    @NotBlank(message = "can't be empty")
    private String body;

}
