package net.seanv.stonegameserver.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import net.seanv.stonegameserver.repositories.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class LoginIsNotTakenValidator implements ConstraintValidator<LoginIsNotTaken, String> {

    private final UserRepository userRepository;

    public LoginIsNotTakenValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean isValid(String str, ConstraintValidatorContext cvc) {
        return str == null || !userRepository.existsByLogin(str);
    }
}
