package io.spring.application.user;

import javax.validation.Constraint;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Constraint(validatedBy = UpdateUserValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface UpdateUserConstraint {

    String message() default "invalid update param";

    Class[] groups() default {};

    Class[] payload() default {};
}
