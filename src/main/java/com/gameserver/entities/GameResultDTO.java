package com.gameserver.entities;

import lombok.Data;
import java.sql.Timestamp;

@Data
public class GameResultDTO {
    private boolean win;
    private String ratingChange;
    private String opponentNickname;
    private Timestamp time;

    public GameResultDTO(GameResult gameResult, User forUser) {
        win = gameResult.getWinner() .equals( forUser );
        ratingChange = (win ? "+" : "-") + gameResult.getRatingChange();
        opponentNickname = gameResult.getWinner(). equals( forUser ) ?
                                                              gameResult.getLoser().getNickname() :
                                                              gameResult.getWinner().getNickname();
        time = gameResult.getTime();
    }
}
