package com.gameserver.entities.responses;

import com.gameserver.game.GameSession;

public record GameResponse(boolean success, String message, byte stage,
                           boolean yourMove, int yourPoints) {

    public static final GameResponse NO_GAME = new GameResponse(false, "No game with this player id", (byte)-1, false, -1);

    public static final byte CHOOSING = 1,
                             GUESSING = 2;


    public GameResponse(int id, GameSession game) { this(true, "ok", id, game); }

    public GameResponse(boolean success, String message, int id, GameSession game) {

        this(success, message, game.getStage(),
             game.getStage()==CHOOSING && id==game.getChoosingPlayer() ||
                      game.getStage()==GUESSING && id==game.getGuessingPlayer(),
             id==game.getChoosingPlayer() ? game.getChoosingPlayerPoints() : game.getGuessingPlayerPoints());
    }
}