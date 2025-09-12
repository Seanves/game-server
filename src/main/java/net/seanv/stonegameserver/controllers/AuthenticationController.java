package net.seanv.stonegameserver.controllers;

import net.seanv.stonegameserver.services.AuthenticationService;
import net.seanv.stonegameserver.dto.auth.AuthResponse;
import net.seanv.stonegameserver.dto.auth.UserAuthDTO;
import net.seanv.stonegameserver.util.UserDTOValidator;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthenticationController {

    private final AuthenticationService authService;
    private final UserDTOValidator userDTOValidator;


    public AuthenticationController(AuthenticationService authService, UserDTOValidator userDTOValidator) {
        this.authService = authService;
        this.userDTOValidator = userDTOValidator;
    }


    @PostMapping("/register")
    public AuthResponse register(@RequestBody @Valid UserAuthDTO userAuthDto, BindingResult bindingResult) {
        userDTOValidator.validate(userAuthDto, bindingResult);
        if (bindingResult.hasErrors()) {
            return new AuthResponse(false, "Errors: " + bindingResult.getAllErrors()
                                                                        .stream()
                                                                        .map(error -> error.getDefaultMessage())
                                                                        .toList(), null);
        }
        return authService.register(userAuthDto);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody UserAuthDTO userAuthDto) {
        return authService.getNewToken(userAuthDto);
    }

}
