package com.gameserver.entities.responses;

import com.gameserver.entities.GameResult;
import com.gameserver.entities.User;
import lombok.Data;
import java.sql.Timestamp;

@Data
public class GameResultDTO {
    private boolean win;
    private int ratingChange;
    private String opponentNickname;
    private Timestamp time;

    public GameResultDTO(GameResult gameResult, User forUser) {
        win = gameResult.getWinner() .equals( forUser );
        ratingChange = win ? gameResult.getWinnerChange() : gameResult.getLoserChange();
        opponentNickname = win ? gameResult.getLoser().getNickname() :
                                 gameResult.getWinner().getNickname();
        time = gameResult.getTime();
    }
}
