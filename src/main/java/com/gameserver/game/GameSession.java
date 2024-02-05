package com.gameserver.game;

import com.gameserver.entities.responses.GameResponse;
import com.gameserver.entities.User;
import com.gameserver.entities.responses.GameResult;
import lombok.Getter;

@Getter
public class GameSession {

    private final Player player1,
                         player2;

    private Player choosingPlayer;

    private int chosenNumber,
                wonId;

    private MoveType moveType;

    private final OnSessionEndCallback callback;
    private final long startTime;
    private long endTime;
    private int ratingChange;


    public enum MoveType {
        CHOOSING,
        GUESSING;
    }

    private static class Player {
        User relatedUser;
        int points;

        Player(User relatedUser) {
            this.relatedUser = relatedUser;
            this.points = 10;
        }

        int getId() { return relatedUser.getId(); }
    }


    public GameSession(User user1, User user2, OnSessionEndCallback callback) {
        player1 = new Player(user1);
        player2 = new Player(user2);
        choosingPlayer = player1;
        moveType = MoveType.CHOOSING;
        wonId = -1;
        startTime = System.currentTimeMillis();
        this.callback = callback;
    }


    public GameResponse makeMoveChoose(int id, int amount) {
        if(isOver())                         { return new GameResponse(false, "Game ended, " + (id==wonId ? "you won" : "you lose"), id, this); }
        if(id != choosingPlayer.getId()
           || moveType != MoveType.CHOOSING) { return new GameResponse(false, "Not your choosing move", id, this); }
        if(amount <= 0)                      { return new GameResponse(false, "Chosen less or equal to zero", id, this); }
        if(amount > choosingPlayer.points)   { return new GameResponse(false, "Chosen more than have", id, this); }

        chosenNumber = amount;
        moveType = MoveType.GUESSING;

        checkIfGameOver();
        return new GameResponse(id, this);
    }

    public GameResponse makeMoveGuess(int id, boolean even) {
        if(isOver())                         { return new GameResponse(false, "Game ended, " + (id== wonId ? "you won" : "you lose"), id, this); }
        if(id != getGuessingPlayer().getId()
           || moveType != MoveType.GUESSING) { return new GameResponse(false, "Not your guessing move", id, this); }

        boolean guessed =  even == (chosenNumber%2 == 0);
        if(guessed) {
            changeChoosingPlayerPoints(-chosenNumber);
            changeGuessingPlayerPoints(+chosenNumber);
        }
        else {
            changeChoosingPlayerPoints(+Math.min(chosenNumber, getGuessingPlayer().points));
            changeGuessingPlayerPoints(-Math.min(chosenNumber, choosingPlayer.points));
        }

        choosingPlayer = getGuessingPlayer();
        moveType = MoveType.CHOOSING;

        checkIfGameOver();
        return new GameResponse(id, this);
    }


    private void changeChoosingPlayerPoints(int n) {
        if(choosingPlayer == player1) {
            player1.points += n;
        }
        else {
            player2.points += n;
        }
    }

    private void changeGuessingPlayerPoints(int n) {
        if(choosingPlayer == player1) {
            player2.points += n;
        }
        else {
            player1.points += n;
        }
    }


    private void checkIfGameOver() {
        if(player1.points <= 0) {
            wonId = player2.getId();
        }
        else if(player2.points <= 0) {
            wonId = player1.getId();
        }
        else { return; }
        endSession();
    }

    public GameResult leave(int id) {
        if(!isOver()) {
            wonId =  id == player1.getId() ? player2.getId() :
                                             player1.getId();
            endSession();
        }
        return new GameResult(id == wonId, id == player1.getId() ? player1.relatedUser.getRating() :
                                                                        player2.relatedUser.getRating(), ratingChange);
    }

    private void endSession() {
        endTime = System.currentTimeMillis();
        ratingChange = callback.onSessionEnd(this);
    }

    private Player getGuessingPlayer() {
        return choosingPlayer == player1 ? player2 : player1;
    }

    public User getWinner() {
        return wonId == player1.getId() ? player1.relatedUser :
               wonId == player2.getId() ? player2.relatedUser :
                   /* game is not over */ null;
    }

    public User getLoser() {
        return wonId == player1.getId() ? player2.relatedUser :
               wonId == player2.getId() ? player1.relatedUser :
                                          null;
    }

    public boolean isMyMove(int id) {
        if(isOver()) { return false; }

        return moveType == MoveType.CHOOSING && id == choosingPlayer.getId()
            || moveType == MoveType.GUESSING && id == getGuessingPlayer().getId();

    }

    public int getPointsById(int id) {
        return id == player1.getId() ? player1.points :
               id == player2.getId() ? player2.points :
                                       -1;
    }

    public User getOpponent(User user) {
        return user.equals(player1.relatedUser) ? player2.relatedUser :
               user.equals(player2.relatedUser) ? player1.relatedUser :
                                                  null;
    }

    public boolean isOver() { return wonId != -1; }

    @Override
    public String toString() {
        return "GameSession{" + "\n  " +
                "player1.points=" + player1.points + ",\n  " +
                "player2.points=" + player2.points + ",\n  " +
                "choosingPlayer.id=" + choosingPlayer.getId() + ",\n  " +
                "chosenNumber=" + chosenNumber + ",\n  " +
                "moveType=" + moveType + "\n" +
                '}';
    }

}
