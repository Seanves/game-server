package com.gameserver.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "Game_result")
@Data
@NoArgsConstructor
public class GameResult {

    public GameResult(User winner, User loser, int winnerChange, int loserChange) {
        this.winner = winner;
        this.loser = loser;
        this.winnerChange = winnerChange;
        this.loserChange = loserChange;
        this.time = Timestamp.valueOf(LocalDateTime.now());
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Setter(AccessLevel.NONE)
    private int resultId;

    @ManyToOne
    @JoinColumn(name = "winner_id", nullable = false)
    private User winner;

    @ManyToOne
    @JoinColumn(name = "loser_id", nullable = false)
    private User loser;

    @Column(name = "winner_change")
    private int winnerChange;

    @Column(name = "loser_change")
    private int loserChange;

    @Column(name = "time")
    private Timestamp time;
}
