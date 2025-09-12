package net.seanv.stonegameserver.repositories;

import net.seanv.stonegameserver.entities.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User,Integer> {
    Optional<User> findById(int id);
    Optional<User> findByLogin(String login);
    boolean existsByLogin(String login);
    boolean existsById(int id);

    @Query(value = """
            SELECT rank
            FROM (
              SELECT id, ROW_NUMBER()
                OVER (ORDER BY rating DESC, max_rating DESC, nickname) AS rank
              FROM Users
            ) AS Ranks
            WHERE id = :userId
            LIMIT 1""", nativeQuery = true)
    Optional<Integer> getRank(int userId);

    @Query(value = """
            FROM User
            ORDER BY rating DESC, maxRating DESC, nickname
            LIMIT 10
            """)
    List<User> getTop10Ranks();

}
