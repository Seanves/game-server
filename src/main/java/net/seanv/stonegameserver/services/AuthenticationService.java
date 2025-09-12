package net.seanv.stonegameserver.services;

import net.seanv.stonegameserver.entities.User;
import net.seanv.stonegameserver.dto.auth.AuthResponse;
import net.seanv.stonegameserver.dto.auth.UserAuthDTO;
import net.seanv.stonegameserver.repositories.UserRepository;
import net.seanv.stonegameserver.security.JWTManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTManager jwtManager;


    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                                 JWTManager jwtManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtManager = jwtManager;
    }


    public AuthResponse register(UserAuthDTO userAuthDto) {
        String encodedPassword = passwordEncoder.encode(userAuthDto.getPassword());
        User user = new User(userAuthDto);
        user.setPassword(encodedPassword);
        userRepository.save(user);

        String token = jwtManager.generate(user.getId());
        return new AuthResponse(true, "ok", token);
    }


    public AuthResponse getNewToken(UserAuthDTO userAuthDto) {
        Optional<User> optional = userRepository.findByLogin(userAuthDto.getLogin());
        if (optional.isPresent() && passwordEncoder.matches(userAuthDto.getPassword(),
                                                            optional.get().getPassword())) {
            String token = jwtManager.generate(optional.get().getId());
            return new AuthResponse(true, "ok", token);
        }
        return new AuthResponse(false, "Wrong login or password", null);
    }
}
