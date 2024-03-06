package com.gameserver.services;

import com.gameserver.entities.User;
import com.gameserver.entities.auth.AuthResponse;
import com.gameserver.entities.auth.UserDTO;
import com.gameserver.repositories.UserRepository;
import com.gameserver.security.JWTManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTManager jwtManager;


    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder, JWTManager jwtManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtManager = jwtManager;
    }


    public AuthResponse register(UserDTO userDto) {
        userDto.setPassword( passwordEncoder.encode(userDto.getPassword()) );
        User user = new User(userDto);
        userRepository.save(user);

        String token = jwtManager.generate(user.getId());
        return new AuthResponse(true, "ok", token);
    }


    public AuthResponse getNewToken(UserDTO userDto) {
        Optional<User> optional = userRepository.findByLogin(userDto.getLogin());
        if(optional.isPresent() && passwordEncoder.matches(userDto.getPassword(), optional.get().getPassword())) {
            String token = jwtManager.generate(optional.get().getId());
            return new AuthResponse(true, "ok", token);
        }
        return new AuthResponse(false, "Wrong login or password", null);
    }
}
