package net.seanv.stonegameserver.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.sql.Timestamp;

@Entity
@Data
@NoArgsConstructor
public class GameResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private int resultId;

    @ManyToOne
    @JoinColumn(name = "winner_id", nullable = false)
    private User winner;

    @ManyToOne
    @JoinColumn(name = "loser_id", nullable = false)
    private User loser;

    private short winnerRatingChange;
    private short loserRatingChange;

    @CreationTimestamp
    private Timestamp time;


    public GameResult(User winner, User loser, int winnerChange, int loserChange) {
        this.winner = winner;
        this.loser = loser;
        this.winnerRatingChange = (short) winnerChange;
        this.loserRatingChange = (short) loserChange;
    }

}
