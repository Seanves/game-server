package com.gameserver.security;

import com.gameserver.entities.User;
import com.gameserver.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;


    public MyUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Optional<User> optional = userRepository.findByLogin(login);
        if(optional.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }

        return new MyUserDetails(optional.get());
    }

    public UserDetails loadUserById(int id) {
        Optional<User> optional = userRepository.findById(id);
        if(optional.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }

        return new MyUserDetails(optional.get());
    }

}
