package io.spring.application.article;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DuplicatedArticleValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DuplicatedArticleConstraint {
    String message() default "article name exists";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
