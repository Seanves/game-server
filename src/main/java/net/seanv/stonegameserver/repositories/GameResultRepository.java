package net.seanv.stonegameserver.repositories;

import net.seanv.stonegameserver.entities.GameResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface GameResultRepository extends JpaRepository<GameResult,Integer> {
    @Query("""
            FROM GameResult
            WHERE winner.id = :userId
                OR loser.id = :userId
            ORDER BY time DESC""")
    Page<GameResult> getResultsPage(int userId, Pageable pageable);
}