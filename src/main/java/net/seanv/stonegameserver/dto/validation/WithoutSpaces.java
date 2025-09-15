package net.seanv.stonegameserver.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Constraint(validatedBy = WithoutSpacesValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WithoutSpaces {
    String message() default "Has space";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
