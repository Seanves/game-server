package com.gameserver.game;


public class GameSession {

    private int player1,
                player2,
                player1points,
                player2points,
                choosingPlayer,
                chosenNumber,
                won;

    private byte stage;
    private final byte CHOOSING = 1,
                       GUESSING = 2;

    public GameSession(int player1, int player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.player1points = 10;
        this.player2points = 10;
        this.choosingPlayer = player1;
        this.won = -1;
        this.stage = CHOOSING;
    }


    public String makeMoveChoose(int id, int amount) {
        if(won!=-1) { return "Game ended, player " + won + " won"; }
        if(id != choosingPlayer || stage != CHOOSING) { return "Not your choosing move"; }
        if(amount <= 0) { return "Chosen less or equal to zero"; }
        if(amount > currentPlayerPoints()) { return "Chosen more than have"; }

        chosenNumber = amount;
        stage = GUESSING;

        checkIfWin();
//        System.out.println(this);
        return this.toString();
    }

    public String makeMoveGuess(int id, boolean even) {
        if(won!=-1) { return "Game ended, player " + won + " won"; }
        if(id != otherPlayer() || stage != GUESSING) { return "Not your guessing move"; }

        boolean guessed =  even == (chosenNumber%2 == 0);
        if(guessed) {
            changeOtherPlayerPoints(+chosenNumber);
            changeCurrentPlayerPoints(-chosenNumber);
        }
        else {
            changeCurrentPlayerPoints(+chosenNumber);
            changeOtherPlayerPoints(-chosenNumber);
        }

        choosingPlayer = otherPlayer();
        stage = CHOOSING;

        checkIfWin();
//        System.out.println(this);
        return this.toString();
    }


    private void changeCurrentPlayerPoints(int n) {
        if(choosingPlayer == player1) {
            player1points += Math.min(n, player2points);
        }
        else {
            player2points += Math.min(n, player1points);
        }
    }

    private void changeOtherPlayerPoints(int n) {
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
            player1points = 0;
        }
        else if(player2points <= 0) {
            won = player1;
            player2points = 0;
        }
    }


    private int otherPlayer() {
        return choosingPlayer ==player1 ? player2 : player1;
    }

    private int currentPlayerPoints() {
        return choosingPlayer ==player1 ? player1points : player2points;
    }

    private int otherPlayerPoints() {
        return choosingPlayer ==player1 ? player2points : player1points;
    }


    @Override
    public String toString() {
        return "GameSession{" + "\n" +
                "player1points=" + player1points + "\n" +
                ", player2points=" + player2points + "\n" +
                ", choosingPlayer=" + choosingPlayer + "\n" +
                ", chosenNumber=" + chosenNumber + "\n" +
                ", stage=" + (stage==1? "choosing" : "guessing") + "\n" +
                '}';
    }
}
