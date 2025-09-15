package net.seanv.stonegameserver.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class WithoutSpacesValidator implements ConstraintValidator<WithoutSpaces, String> {

    @Override
    public boolean isValid(String str, ConstraintValidatorContext cvc) {
        return str == null || !str.contains(" ");
    }
}
