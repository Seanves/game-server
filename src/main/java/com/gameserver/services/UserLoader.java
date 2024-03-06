package com.gameserver.services;

import com.gameserver.entities.User;
import com.gameserver.repositories.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class UserLoader {

    private final UserRepository userRepository;

    public UserLoader(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public User load(int id) {
        return userRepository.findById(id).orElseThrow( () ->
                              new RuntimeException("User with id " + id + "not found"));

    }

}
