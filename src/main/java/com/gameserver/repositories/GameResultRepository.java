package com.gameserver.repositories;

import com.gameserver.entities.GameResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GameResultRepository extends JpaRepository<GameResult,Integer> {
    @Query(value = "SELECT gr.* " +
                   "FROM Game_result AS gr " +
                   "JOIN Users AS w ON gr.winner_id = w.id " +
                   "JOIN Users AS l ON gr.loser_id = l.id " +
                   "WHERE :id IN (w.id, l.id) " +
                   "ORDER BY time DESC", nativeQuery = true)
    Page<GameResult> getPageForUserId(@Param("id") int id, Pageable pageable);
}
