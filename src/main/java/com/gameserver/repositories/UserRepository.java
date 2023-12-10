package com.gameserver.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.gameserver.entities.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Integer> {
    Optional<User> findById(int id);
    Optional<User> findByLogin(String login);
    Optional<User> findByLoginAndPassword(String login, String password);
    Boolean existsByLogin(String login);
}
