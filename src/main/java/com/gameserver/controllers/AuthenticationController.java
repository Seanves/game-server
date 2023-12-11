package com.gameserver.controllers;

import com.gameserver.entities.User;
import com.gameserver.entities.auth.UserDto;
import com.gameserver.services.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.gameserver.security.MyUserDetails;
import java.util.Map;
import java.util.Optional;

@RestController
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @PostMapping("/register")
    public Map<String,String> register(@RequestBody UserDto userDto) {
        User user = new User(userDto.getLogin(), passwordEncoder.encode(userDto.getPassword()));
        return authenticationService.register(user);
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody UserDto userDto) {
        Optional<User> optional = authenticationService.getUserByLogin(userDto.getLogin());
        if(optional.isPresent() && passwordEncoder.matches(userDto.getPassword(), optional.get().getPassword())) {
            return authenticationService.getNewToken(userDto.getLogin());
        }
        return Map.of("Error", "Wrong login or password");
    }


    @PostMapping("/get")
    public String test() {
        System.out.println(((MyUserDetails)(SecurityContextHolder.getContext().getAuthentication().getPrincipal())).getUser());
        return ((MyUserDetails)(SecurityContextHolder.getContext().getAuthentication().getPrincipal())).getUser().toString();
    }

}
