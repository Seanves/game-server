package com.gameserver.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.gameserver.entities.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Integer> {
    Optional<User> findById(int id);
    Optional<User> findByLogin(String login);
    Optional<User> findByLoginAndPassword(String login, String password);
    Boolean existsByLogin(String login);

    @Query(value =  "SELECT position " +
                    "FROM ( " +
                    "    SELECT id, RANK() OVER (ORDER BY rating DESC) AS position " +
                    "    FROM Users " +
                    ") AS ranked_users " +
                    "WHERE id = :userId", nativeQuery = true)
    Integer rank(@Param("userId") int userId);
}
