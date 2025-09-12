package net.seanv.stonegameserver.dto.responses;

import lombok.Getter;
import net.seanv.stonegameserver.entities.GameResult;
import java.sql.Timestamp;

@Getter
public class GameResultDTO {
    private final boolean win;
    private final int ratingChange;
    private final String opponentNickname;
    private final Timestamp time;

    public GameResultDTO(GameResult gameResult, int forUserId) {
        win = (gameResult.getWinner().getId() == forUserId);
        if (win) {
            ratingChange = gameResult.getWinnerRatingChange();
            opponentNickname = gameResult.getLoser().getNickname();
        } else {
            ratingChange = gameResult.getLoserRatingChange();
            opponentNickname = gameResult.getWinner().getNickname();
        }
        time = gameResult.getTime();
    }
}
