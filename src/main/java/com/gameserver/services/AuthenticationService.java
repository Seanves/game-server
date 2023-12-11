package com.gameserver.services;

import com.gameserver.entities.User;
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
    private UserRepository userRepository;

    @Autowired
    private JWTManager jwtManager;


    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Optional<User> optional = userRepository.findByLogin(login);
        if(optional.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }

        return new MyUserDetails(optional.get());
    }


    public Map<String,String> register(User user) {
        if(isExists(user.getLogin())) {
            return Map.of("Error", "Login already taken");
        }
        userRepository.save(user);

        String token = jwtManager.generate(user.getLogin());
        return Map.of("token", token);
    }


    public Map<String, String> getNewToken(String login) {
        if(isExists(login)) {
            String token = jwtManager.generate(login);
            return Map.of("token", token);
        }
        return Map.of("Error", "Login doesnt exit");
    }


    public boolean isExists(String login) {
        return userRepository.existsByLogin(login);
    }

    public Optional<User> getUserByLogin(String login) {
        return userRepository.findByLogin(login);
    }
}
