package com.gameserver.responses;

import com.gameserver.game.GameSession;

public record GameResponse(boolean success, String message, byte stage,
                           boolean yourMove, int yourPoints) {

    public static final GameResponse NOT_YOU_CHOOSING_MOVE = new GameResponse(false, "Not your choosing move"),
                                   NOT_YOUR_GUESSING_MOVE = new GameResponse(false, "Not your guessing move"),
                                   NO_GAME = new GameResponse(false, "No game with this player id"),
                                   CHOSEN_MORE_THAN_HAVE = new GameResponse(false, "Chosen more than have") ,
                                   CHOSEN_ZERO_OR_LESS = new GameResponse(false, "Chosen less or equal to zero"),
                                   YOU_WON = new GameResponse(false, "Game ended, you won"),
                                   YOU_LOSE = new GameResponse(false, "Game ended, you won");

    public static final byte CHOOSING = 1,
                             GUESSING = 2;


    public GameResponse(int id, GameSession game) { this(true, "ok", id, game); }

    public GameResponse(boolean success, String message) {
        this(success, message, (byte)-1, false, -1);
    }

    public GameResponse(boolean success, String message, int id, GameSession game) {

        this(success, message, game.getStage(),
             game.getStage()==CHOOSING && id==game.getChoosingPlayer() ||
                      game.getStage()==GUESSING && id==game.getGuessingPlayer(),
             id==game.getChoosingPlayer() ? game.getChoosingPlayerPoints() : game.getGuessingPlayerPoints());
    }
}