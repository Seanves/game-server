package com.gameserver.services;

import com.gameserver.entities.User;
import com.gameserver.entities.auth.AuthResponse;
import com.gameserver.security.JWTManager;
import com.gameserver.security.MyUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthenticationService implements UserDetailsService {

    @Autowired
    private JWTManager jwtManager;

    @Autowired
    private UserService userService;


    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Optional<User> optional = userService.getUser(login);
        if(optional.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }

        return new MyUserDetails(optional.get());
    }

    public UserDetails loadUserById(int id) {
        Optional<User> optional = userService.getUser(id);
        if(optional.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }

        return new MyUserDetails(optional.get());
    }


    public AuthResponse register(User user) {
        if(userService.isExists(user.getLogin())) {
            return new AuthResponse(false, "Login already taken", null);
        }
        userService.save(user);

        String token = jwtManager.generate(user.getId());
        return new AuthResponse(true, "ok", token);
    }


    public AuthResponse getNewToken(int id) {
        if(userService.isExists(id)) {
            String token = jwtManager.generate(id);
            return new AuthResponse(true, "ok", token);
        }
        return new AuthResponse(false, "Login doesnt exit", null);
    }

}
