package net.seanv.stonegameserver.dto.responses;

import net.seanv.stonegameserver.game.GameSession;


public record GameResponse(boolean success, String message, GameSession.TurnType turnType,
                           boolean yourTurn, int yourPoints, boolean gameOver, boolean win) {

    public static final GameResponse NOT_IN_GAME = new GameResponse(false, "You are not in game", null,
                                                            false, -1, true, false);


    public GameResponse(int userId, GameSession game) { this(true, "ok", userId, game); }

    public GameResponse(boolean success, String message, int userId, GameSession game) {
        this(success, message,
                /* turnType: */ game.getTurnType(),
                /* yourTurn: */ game.isMyTurn(userId),
                /* yourPoints: */ game.getPointsById(userId),
                /* gameOver: */ game.isOver(),
                /* win: */ userId == game.getWonId()
             );
    }

}