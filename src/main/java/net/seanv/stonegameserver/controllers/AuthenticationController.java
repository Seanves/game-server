package net.seanv.stonegameserver.controllers;

import net.seanv.stonegameserver.services.AuthenticationService;
import net.seanv.stonegameserver.dto.auth.AuthResponse;
import net.seanv.stonegameserver.dto.auth.UserAuthDTO;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthenticationController {

    private final AuthenticationService authService;


    public AuthenticationController(AuthenticationService authService) {
        this.authService = authService;
    }


    @PostMapping("/register")
    public AuthResponse register(@RequestBody @Valid UserAuthDTO userAuthDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new AuthResponse(false, "Validation errors: " + bindingResult.getFieldErrors()
                                                        .stream()
                                                        .map(e -> e.getField() + ": " + e.getDefaultMessage())
                                                        .toList(), null);
        }
        return authService.register(userAuthDto);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody UserAuthDTO userAuthDto) {
        return authService.getNewToken(userAuthDto);
    }

}
