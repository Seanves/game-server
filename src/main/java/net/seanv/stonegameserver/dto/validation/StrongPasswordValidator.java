package net.seanv.stonegameserver.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {
    private static final Pattern pattern = Pattern.compile(
            "^(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d!@#$%^&*_-]{8,20}$");


    @Override
    public boolean isValid(String str, ConstraintValidatorContext constraintValidatorContext) {
        return str != null && pattern.matcher(str).matches();
    }
}
