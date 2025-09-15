package net.seanv.stonegameserver.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MaxCapsValidator implements ConstraintValidator<MaxCaps, String> {
    private int maxPercentage;

    @Override
    public void initialize(MaxCaps constraintAnnotation) {
        maxPercentage = constraintAnnotation.percentage();
        if (maxPercentage < 0 || maxPercentage > 100) {
            throw new IllegalArgumentException("@MaxCaps.percentage() = " + maxPercentage);
        }
    }

    @Override
    public boolean isValid(String str, ConstraintValidatorContext cvc) {
        if (str == null) { return true; }

        int upperCaseCount = 0;
        int lowerCaseCount = 0;

        for (char c: str.toCharArray()) {
            if (Character.isLetter(c)) {
                if (Character.isUpperCase(c)) { upperCaseCount++; }
                else                          { lowerCaseCount++; }
            }
        }
        int letters = upperCaseCount + lowerCaseCount;
        double percentage = (double) upperCaseCount / letters * 100;

        return percentage <= maxPercentage;
    }
}
