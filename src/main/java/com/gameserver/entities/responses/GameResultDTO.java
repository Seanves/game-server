package com.gameserver.entities.responses;

import com.gameserver.entities.GameResult;
import lombok.Data;
import java.sql.Timestamp;

@Data
public class GameResultDTO {
    private boolean win;
    private int ratingChange;
    private String opponentNickname;
    private Timestamp time;

    public GameResultDTO(GameResult gameResult, int forUserId) {
        win = gameResult.getWinner().getId() == forUserId;
        ratingChange = win ? gameResult.getWinnerChange() : gameResult.getLoserChange();
        opponentNickname = win ? gameResult.getLoser().getNickname() :
                                 gameResult.getWinner().getNickname();
        time = gameResult.getTime();
    }
}
