package net.seanv.stonegameserver.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = MaxCapsValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MaxCaps {
    int percentage();

    String message() default "Has space";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
