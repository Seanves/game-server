package net.seanv.stonegameserver.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Constraint(validatedBy = LoginIsNotTakenValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginIsNotTaken {
    String message() default "Login is already taken";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
