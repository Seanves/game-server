package com.gameserver.security;

import com.gameserver.entities.User;
import com.gameserver.services.UserLoader;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedUserContext {

    private final UserLoader userLoader;

    public AuthenticatedUserContext(UserLoader userLoader) {
        this.userLoader = userLoader;
    }


    public int getId() {
        return ((MyPrincipal)(SecurityContextHolder.getContext()
                                                   .getAuthentication()
                                                   .getPrincipal()))
                                                   .getId();
    }

    public User loadUser() {
        return userLoader.load(getId());
    }

}
