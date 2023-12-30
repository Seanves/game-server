package com.gameserver.controllers;

import com.gameserver.entities.User;
import com.gameserver.entities.auth.AuthResponse;
import com.gameserver.entities.auth.UserDto;
import com.gameserver.services.AuthenticationService;
import com.gameserver.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService, UserService userService, PasswordEncoder passwordEncoder) {
        this.authenticationService = authenticationService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }


    @PostMapping("/register")
    public AuthResponse register(@RequestBody @Valid UserDto userDto, BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            return new AuthResponse(false, "Errors: " + bindingResult.getFieldErrors()
                                                 .stream()
                                                 .map((error) -> error.getDefaultMessage())
                                                 .toList(), null);
        }
        User user = new User(userDto);
        return authenticationService.register(user);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody UserDto userDto) {
        Optional<User> optional = userService.getUser(userDto.getLogin());
        if(optional.isPresent() && passwordEncoder.matches(userDto.getPassword(), optional.get().getPassword())) {
            return authenticationService.getNewToken(optional.get().getId());
        }
        return new AuthResponse(false, "Wrong login or password", null);
    }

}
