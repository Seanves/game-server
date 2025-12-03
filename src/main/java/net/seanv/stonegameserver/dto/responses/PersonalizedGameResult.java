package net.seanv.stonegameserver.dto.responses;

import lombok.Getter;
import net.seanv.stonegameserver.entities.GameResult;
import java.sql.Timestamp;

@Getter
public class PersonalizedGameResult {
    private final boolean win;
    private final int ratingBefore;
    private final int ratingAfter;
    private final Opponent opponent;
    private final Timestamp time;

    public PersonalizedGameResult(GameResult result, int userId) {
        if (userId == result.getWinner().getId()) {
            win = true;
            ratingBefore = result.getWinnerRatingBefore();
            ratingAfter = result.getWinnerRatingAfter();
            opponent = new Opponent(result.getLoser());
        } else if (userId == result.getLoser().getId()) {
            win = false;
            ratingBefore = result.getLoserRatingBefore();
            ratingAfter = result.getLoserRatingAfter();
            opponent = new Opponent(result.getWinner());
        } else {
            throw new IllegalArgumentException("userId = " + userId + " " + result);
        }

        time = result.getTime();
    }

    public int getRatingChange() {
        return ratingAfter - ratingBefore;
    }
}
