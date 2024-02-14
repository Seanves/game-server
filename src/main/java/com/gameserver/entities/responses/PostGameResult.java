package com.gameserver.entities.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class PostGameResult {
    private boolean win;
    private int currentRating;
    private int change;
}
