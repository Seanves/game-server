package com.gameserver.controllers;

import com.gameserver.entities.User;
import com.gameserver.entities.auth.AuthResponse;
import com.gameserver.entities.auth.UserDto;
import com.gameserver.security.JWTManager;
import com.gameserver.services.AuthenticationService;
import com.gameserver.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import com.gameserver.security.MyUserDetails;
import java.util.Optional;
import org.springframework.validation.FieldError;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestController
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JWTManager jwtManager; //debug

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService, UserService userService, PasswordEncoder passwordEncoder, JWTManager jwtManager) {
        this.authenticationService = authenticationService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtManager = jwtManager;
    }


    @PostMapping("/register")
    public AuthResponse register(@RequestBody @Valid UserDto userDto, BindingResult bindingResult) {
        if(userDto.getNickname()==null) { new AuthResponse(false, "Nickname is null", null); }
        if(bindingResult.hasErrors()) {
            return new AuthResponse(false, "Errors: " + bindingResult.getFieldErrors()
                                                 .stream()
                                                 .map((FieldError error) -> error.getDefaultMessage())
                                                 .toList(), null);
        }
        User user = new User(userDto.getLogin(), passwordEncoder.encode(userDto.getPassword()), userDto.getNickname());
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


    @PostMapping("/user")
    public String test() {
        System.out.println("token:         " + ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getHeader("Authorization").substring(7));
        System.out.println("id from token: " + jwtManager.validateAndGetId(((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getHeader("Authorization").substring(7)));
        System.out.println("user from db:  " + ((MyUserDetails)(SecurityContextHolder.getContext().getAuthentication().getPrincipal())).getUser());
        return ((MyUserDetails)(SecurityContextHolder.getContext().getAuthentication().getPrincipal())).getUser().toString();
    }

}
