package com.gameserver.entities.responses;

import com.gameserver.game.GameSession;


public record GameResponse(boolean success, String message, byte stage,
                           boolean yourMove, int yourPoints, boolean gameOver, boolean win) {

    public static final GameResponse NO_GAME = new GameResponse(false, "No game with this player id", (byte)-1, false, -1, false, false);
    public static final byte CHOOSING = 1,
                             GUESSING = 2;


    public GameResponse(int id, GameSession game) { this(true, "ok", id, game); }

    public GameResponse(boolean success, String message, int id, GameSession game) {
        // unable to write anything before constructor call
        this(success, message, game.getStage(),
                !game.isOver() &&
                (game.getStage()==CHOOSING && id==game.getChoosingPlayer() ||
                 game.getStage()==GUESSING && id==game.getGuessingPlayer()),
                 id==game.getChoosingPlayer() ? game.getChoosingPlayerPoints() : game.getGuessingPlayerPoints(),
                 game.getWon()!=-1, id==game.getWon());
    }

//    public boolean isGameOver() {
//        return yourPoints>=20 || yourPoints<=0;
//    }
//
//    public boolean isWin() {
//        return yourPoints>=20;
//    }
}