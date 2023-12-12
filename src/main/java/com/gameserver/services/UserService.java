package com.gameserver.services;

import com.gameserver.entities.User;
import com.gameserver.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public boolean isExists(String login) {
        return userRepository.existsByLogin(login);
    }

    public Optional<User> getUserByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    public void changeNickname(User user, String nickname) {
        user.setNickname(nickname);
        save(user);
    }

    public int getRank(User user) {
        return userRepository.rank(user.getId());
    }

    public void save(User user) {
        userRepository.save(user);
    }

}
