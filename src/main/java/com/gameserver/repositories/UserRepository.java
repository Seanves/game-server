package com.gameserver.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.gameserver.entities.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Integer> {
    Optional<User> findById(int id);
    Optional<User> findByLogin(String login);
//    Optional<User> findByLoginAndPassword(String login, String password);
    Boolean existsByLogin(String login);
    Boolean existsById(int id);


    @Query(value = "SELECT id, position " +
                   "FROM ( " +
                   "    SELECT id, RANK() OVER (ORDER BY rating DESC) AS position " +
                   "    FROM Users " +
                   ") AS ranked_users", nativeQuery = true)
    List<Integer[]> ranks();

    @Query(value = "SELECT position, nickname, rating " +
                   "FROM ( " +
                   "    SELECT nickname, rating, RANK() OVER (ORDER BY rating DESC) AS position " +
                   "    FROM Users " +
                   ") AS ranked_users " +
                   "LIMIT 10", nativeQuery = true)
    List<Object[]> top10ranks();
}
