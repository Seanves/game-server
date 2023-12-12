package com.gameserver.game;

import com.gameserver.entities.responses.GameResponse;
import com.gameserver.entities.User;
import com.gameserver.entities.responses.GameResult;
import lombok.Getter;

@Getter
public class GameSession {

    private int player1,
                player2,
                player1points,
                player2points,
                choosingPlayer,
                chosenNumber,
                won;

    private byte stage;
    public final byte CHOOSING = 1,
                      GUESSING = 2;

    private final User user1,
                       user2;

    private final OnSessionEndCallback callback;
    private long endTime;
    private int ratingChange;

    public GameSession(User user1, User user2, OnSessionEndCallback callback) {
        this.user1 = user1;
        this.user2 = user2;
        this.player1 = user1.getId();
        this.player2 = user2.getId();
        this.player1points = 10;
        this.player2points = 10;
        this.choosingPlayer = player1;
        this.stage = CHOOSING;
        this.won = -1;
        this.callback = callback;
    }


    public GameResponse makeMoveChoose(int id, int amount) {
        if(won!=-1) { return new GameResponse(false, "Game ended, " + (id==won? "you won" : "you lose"), id, this); }
        if(id != choosingPlayer || stage != CHOOSING) { return new GameResponse(false, "Not your choosing move", id, this); }
        if(amount <= 0) { return new GameResponse(false, "Chosen less or equal to zero", id, this); }
        if(amount > getChoosingPlayerPoints()) { return new GameResponse(false, "Chosen more than have", id, this); }

        chosenNumber = amount;
        stage = GUESSING;

        checkIfWin();
        return new GameResponse(id, this);
    }

    public GameResponse makeMoveGuess(int id, boolean even) {
        if(won!=-1) { return new GameResponse(false, "Game ended, " + (id==won? "you won" : "you lose"), id, this); }
        if(id != getGuessingPlayer() || stage != GUESSING) { return new GameResponse(false, "Not your guessing move", id, this); }

        boolean guessed =  even == (chosenNumber%2 == 0);
        if(guessed) {
            changeChoosingPlayerPoints(-chosenNumber);
            changeGuessingPlayerPoints(+chosenNumber);
        }
        else {
            changeChoosingPlayerPoints(+Math.min(chosenNumber, getGuessingPlayerPoints()));
            changeGuessingPlayerPoints(-Math.min(chosenNumber, getGuessingPlayerPoints()));
        }

        choosingPlayer = getGuessingPlayer();
        stage = CHOOSING;

        checkIfWin();
        return new GameResponse(id, this);
    }


    private void changeChoosingPlayerPoints(int n) {
        if(choosingPlayer == player1) {
            player1points += n;
        }
        else {
            player2points += n;
        }
    }

    private void changeGuessingPlayerPoints(int n) {
        if(choosingPlayer == player1) {
            player2points += n;
        }
        else {
            player1points += n;
        }
    }


    private void checkIfWin() {
        if(player1points <= 0) {
            won = player2;
        }
        else if(player2points <= 0) {
            won = player1;
        }
        else { return; }
        endSession();
    }

    public GameResult leave(int id) {
        if(!isOver()) {
            won =  id==player1? player2 : player1;
            endSession();
        }
        return new GameResult(id==won, id==player1? user1.getRating() :
                                                           user2.getRating(), ratingChange);
    }

    private void endSession() {
        endTime = System.currentTimeMillis();
        ratingChange = callback.onSessionEnd(this);
    }


    public int getGuessingPlayer() { return choosingPlayer==player1 ? player2 : player1; }

    public int getChoosingPlayerPoints() {
        return choosingPlayer==player1 ? player1points : player2points;
    }

    public int getGuessingPlayerPoints() {
        return choosingPlayer==player1 ? player2points : player1points;
    }

    public User getWinner() { return won!=-1 ? won==player1? user1 : user2
                                             : null; }

    public User getLoser() { return won!=-1 ? won==player1? user2 : user1
                                            : null; }

    public boolean isOver() { return won!=-1; }

    @Override
    public String toString() {
        return "GameSession{" + "\n  " +
                "player1points=" + player1points + ",\n  " +
                "player2points=" + player2points + ",\n  " +
                "choosingPlayer=" + choosingPlayer + ",\n  " +
                "chosenNumber=" + chosenNumber + ",\n  " +
                "stage=" + (stage==1? "choosing" : "guessing") + "\n" +
                '}';
    }
}
