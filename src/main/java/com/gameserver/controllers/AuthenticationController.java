package com.gameserver.controllers;

import com.gameserver.entities.auth.UserDto;
import com.gameserver.repositories.UserRepository;
import com.gameserver.security.JWTManager;
import com.gameserver.services.AuthenticationService;
import org.apache.catalina.authenticator.SpnegoAuthenticator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import com.gameserver.entities.User;
import com.gameserver.security.MyUserDetails;

import java.util.Map;
import java.util.Optional;

@RestController
public class AuthenticationController {

    @Autowired
    private JWTManager jwtManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationService authenticationService;


    @PostMapping("/register")
    public Map<String,String> register(@RequestBody UserDto userDto, BindingResult bindingResult) {
        if(authenticationService.isExists(userDto.getLogin())) {
            return Map.of("Error", "Login already taken");
        }

        User user = new User(userDto.getLogin(), passwordEncoder.encode(userDto.getPassword()));
        userRepository.save(user);

        String token = jwtManager.generate(user.getLogin());
        return Map.of("token", token);
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody UserDto userDto) {
        Optional<User> optional = userRepository.findByLogin(userDto.getLogin());
        if(optional.isPresent() && passwordEncoder.matches(userDto.getPassword(), optional.get().getPassword())) {
            String token = jwtManager.generate(userDto.getLogin());
            return Map.of("token", token);
        }
        return Map.of("Error", "Wrong login or password");
    }


    @PostMapping("/get")
    public String test() {
        System.out.println(((MyUserDetails)(SecurityContextHolder.getContext().getAuthentication().getPrincipal())).getUser());
        return ((MyUserDetails)(SecurityContextHolder.getContext().getAuthentication().getPrincipal())).getUser().toString();
    }

}
