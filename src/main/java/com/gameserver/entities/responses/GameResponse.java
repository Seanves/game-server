package com.gameserver.entities.responses;

import com.gameserver.game.GameSession;


public record GameResponse(boolean success, String message, GameSession.MoveType moveType,
                           boolean yourMove, int yourPoints, boolean gameOver, boolean win) {

    public static final GameResponse NOT_IN_GAME = new GameResponse(false, "You are not in game", null, false, -1, true, false);


    public GameResponse(int id, GameSession game) { this(true, "ok", id, game); }

    public GameResponse(boolean success, String message, int id, GameSession game) {
        // unable to write anything before constructor call
        this(success, message,
                /* moveType: */ game.getMoveType(),
                /* yourMove: */ game.isMyMove(id),
                /* yourPoints: */ game.getPointsById(id),
                /* gameOver: */ game.isOver(),
                /* win: */ id == game.getWonId()
             );
    }

}