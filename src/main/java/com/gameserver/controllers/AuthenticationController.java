package com.gameserver.controllers;

import com.gameserver.services.AuthenticationService;
import com.gameserver.entities.auth.AuthResponse;
import com.gameserver.entities.auth.UserDTO;
import com.gameserver.util.UserDTOValidator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final UserDTOValidator userDTOValidator;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService, UserDTOValidator userDTOValidator) {
        this.authenticationService = authenticationService;
        this.userDTOValidator = userDTOValidator;
    }


    @PostMapping("/register")
    public AuthResponse register(@RequestBody @Valid UserDTO userDto, BindingResult bindingResult) {
        userDTOValidator.validate(userDto, bindingResult);
        if(bindingResult.hasErrors()) {
            return new AuthResponse(false, "Errors: " + bindingResult.getAllErrors()
                                                                        .stream()
                                                                        .map(error -> error.getDefaultMessage())
                                                                        .toList(), null);
        }
        return authenticationService.register(userDto);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody UserDTO userDto) {
        return authenticationService.getNewToken(userDto);
    }

}
