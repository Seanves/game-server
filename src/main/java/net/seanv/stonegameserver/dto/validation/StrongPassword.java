package net.seanv.stonegameserver.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Constraint(validatedBy = StrongPasswordValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface StrongPassword {
    String message() default "Only english letters, digits and !@#$%^&*-_ , " +
                             "at least one uppercase letter and digit, length 8-20";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}