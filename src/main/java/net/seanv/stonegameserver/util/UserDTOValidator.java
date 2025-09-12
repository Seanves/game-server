package net.seanv.stonegameserver.util;

import net.seanv.stonegameserver.dto.auth.UserAuthDTO;
import net.seanv.stonegameserver.repositories.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class UserDTOValidator implements Validator {

    private final UserRepository userRepository;

    public UserDTOValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public boolean supports(Class<?> clazz) {
        return UserAuthDTO.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        UserAuthDTO userAuthDto = (UserAuthDTO) target;

        if (userRepository.existsByLogin(userAuthDto.getLogin())) {
            errors.reject("", "Login already taken");
        }

        if (userAuthDto.getLogin().contains(" ")) {
            errors.reject("", "Login mustn't contain spaces");
        }

        if (userAuthDto.getPassword() .equals (userAuthDto.getLogin())) {
            errors.reject("", "Password the same as login");
        }

        if (userAuthDto.getPassword() .equals (userAuthDto.getNickname())) {
            errors.reject("", "Password the same as nickname");
        }

        validateNickname(userAuthDto.getNickname(), errors);

    }

    public void validateNickname(String nickname, Errors errors) {
        if (nickname.contains(" ")) {
            errors.reject("", "Nickname mustn't contain spaces");
        }

        if (capsPercentage(nickname) > 50) {
            errors.reject("", "To many uppercase letters");
        }

    }


    private double capsPercentage(String string) {
        int upperCaseCount = 0;
        int lowerCaseCount = 0;

        for (char c: string.toCharArray()) {
            if (Character.isLetter(c)) {
                if (Character.isUpperCase(c)) { upperCaseCount++; }
                else                          { lowerCaseCount++; }
            }
        }
        int letters = upperCaseCount + lowerCaseCount;

        return (double) upperCaseCount / letters * 100;
    }

}
