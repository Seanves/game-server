package net.seanv.stonegameserver.security;

import net.seanv.stonegameserver.entities.User;
import net.seanv.stonegameserver.repositories.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthUserContext {

    private final UserRepository userRepository;

    public AuthUserContext(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public int getId() {
        try {
            return ((PrincipalImpl)(SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getPrincipal()))
                    .getId();
        } catch (Exception e) {
            throw new RuntimeException("failed to get user id", e);
        }
    }

    public User loadUser() {
        int id = getId();
        return userRepository.findById(id).orElseThrow( () ->
                new RuntimeException("user with id " + id + " not found"));
    }

}
