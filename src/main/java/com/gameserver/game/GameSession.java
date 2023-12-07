package com.gameserver.game;

import com.gameserver.responses.GameResponse;
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

    public GameSession(int player1, int player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.player1points = 10;
        this.player2points = 10;
        this.choosingPlayer = player1;
        this.stage = CHOOSING;
        this.won = -1;
    }


    public GameResponse makeMoveChoose(int id, int amount) {
        if(won!=-1) { return new GameResponse(false, "Game ended, " + (id==won? "you won" : "you lose"), id, this); }
        if(id != choosingPlayer || stage != CHOOSING) { return new GameResponse(false, "Not your choosing move", id, this); }
        if(amount <= 0) { return new GameResponse(false, "Chosen less or equal to zero", id, this); }
        if(amount > getChoosingPlayerPoints()) { return new GameResponse(false, "Chosen more than have", id, this); }

        chosenNumber = amount;
        stage = GUESSING;

        checkIfWin();
        System.out.println(this);
        return new GameResponse(id, this);
    }

    public GameResponse makeMoveGuess(int id, boolean even) {
        if(won!=-1) { return new GameResponse(false, "Game ended, " + (id==won? "you won" : "you lose"), id, this); }
        if(id != getGuessingPlayer() || stage != GUESSING) { return new GameResponse(false, "Not your guessing move", id, this); }

        boolean guessed =  even == (chosenNumber%2 == 0);
        if(guessed) {
            changeGuessingPlayerPoints(+chosenNumber);
            changeChoosingPlayerPoints(-chosenNumber);
        }
        else {
            changeChoosingPlayerPoints(+chosenNumber);
            changeGuessingPlayerPoints(-chosenNumber);
        }

        choosingPlayer = getGuessingPlayer();
        stage = CHOOSING;

        checkIfWin();
        System.out.println(this);
        return new GameResponse(id, this);
    }


    private void changeChoosingPlayerPoints(int n) {
        if(choosingPlayer == player1) {
            player1points += Math.min(n, player2points);
        }
        else {
            player2points += Math.min(n, player1points);
        }
    }

    private void changeGuessingPlayerPoints(int n) {
        if(choosingPlayer == player1) {
            player2points += Math.min(n, player1points);
        }
        else {
            player1points += Math.min(n, player2points);
        }
    }


    private void checkIfWin() {
        if(player1points <= 0) {
            won = player2;
        }
        else if(player2points <= 0) {
            won = player1;
        }
    }


    public int getGuessingPlayer() { return choosingPlayer==player1 ? player2 : player1; }

    public int getChoosingPlayerPoints() {
        return choosingPlayer==player1 ? player1points : player2points;
    }

    public int getGuessingPlayerPoints() {
        return choosingPlayer==player1 ? player2points : player1points;
    }


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
