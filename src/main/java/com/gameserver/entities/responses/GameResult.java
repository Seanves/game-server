package com.gameserver.entities.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class GameResult {
    private boolean win;
    private int currentRating;
    private int change;
}
