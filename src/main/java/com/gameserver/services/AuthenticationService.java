package com.gameserver.services;


import com.gameserver.entities.User;
import com.gameserver.repositories.UserRepository;
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
    private UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Optional<User> optional = userRepository.findByLogin(login);
        if(optional.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }

        return new MyUserDetails(optional.get());
    }

    public boolean isExists(String login) {
        return userRepository.existsByLogin(login);
    }
}
