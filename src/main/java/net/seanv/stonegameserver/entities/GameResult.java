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

    private int winnerRatingBefore;
    private int winnerRatingAfter;
    private int loserRatingBefore;
    private int loserRatingAfter;

    @CreationTimestamp
    private Timestamp time;


    public GameResult(User winner, int winnerRatingBefore, User loser, int loserRatingBefore) {
        this.winner = winner;
        this.loser = loser;
        this.winnerRatingBefore = winnerRatingBefore;
        this.winnerRatingAfter = winner.getRating();
        this.loserRatingBefore = loserRatingBefore;
        this.loserRatingAfter = loser.getRating();
    }


    public int getWinnerRatingChange() {
        return winnerRatingAfter - winnerRatingBefore;
    }

    public int getLoserRatingChange() {
        return loserRatingAfter - loserRatingBefore;
    }

}
