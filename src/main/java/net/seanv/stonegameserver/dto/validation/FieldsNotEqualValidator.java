package net.seanv.stonegameserver.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ValidationException;
import org.springframework.beans.BeanWrapperImpl;

public class FieldsNotEqualValidator implements ConstraintValidator<FieldsNotEqual, Object> {

    private String field1;
    private String field2;
    private String message;

    public void initialize(FieldsNotEqual constraintAnnotation) {
        this.field1 = constraintAnnotation.field1();
        this.field2 = constraintAnnotation.field2();
        if (constraintAnnotation.message().isEmpty()) {
            message = field1 + " must not be equal to " + field2;
        } else {
            message = constraintAnnotation.message();
        }
    }

    public boolean isValid(Object object, ConstraintValidatorContext context) {
        Object field1Value, field2Value;

        try {
            BeanWrapperImpl beanWrapper = new BeanWrapperImpl(object);
            field1Value = beanWrapper.getPropertyValue(field1);
            field2Value = beanWrapper.getPropertyValue(field2);
        } catch (Exception e) {
            throw new ValidationException("failed to get values from fields " + field1 + ", " + field2
                                        + " from object " + object, e);
        }


        boolean isValid = (field1Value != null && !field1Value.equals(field2Value));

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message)
                    .addPropertyNode(field1)
                    .addConstraintViolation();
        }

        return isValid;
    }
}
