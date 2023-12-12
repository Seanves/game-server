package com.gameserver.services;

import com.gameserver.entities.User;
import com.gameserver.entities.auth.AuthResponse;
import com.gameserver.repositories.UserRepository;
import com.gameserver.security.JWTManager;
import com.gameserver.security.MyUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthenticationService implements UserDetailsService {

    @Autowired
    private JWTManager jwtManager;

    @Autowired
    private UserService userService;


    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Optional<User> optional = userService.getUserByLogin(login);
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

        String token = jwtManager.generate(user.getLogin());
        return new AuthResponse(true, "ok", token);
    }


    public AuthResponse getNewToken(String login) {
        if(userService.isExists(login)) {
            String token = jwtManager.generate(login);
            return new AuthResponse(true, "ok", token);
        }
        return new AuthResponse(false, "Login doesnt exit", null);
    }

}
