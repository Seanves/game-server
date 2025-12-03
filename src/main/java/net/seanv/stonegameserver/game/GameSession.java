package net.seanv.stonegameserver.game;

import net.seanv.stonegameserver.dto.responses.GameResponse;
import net.seanv.stonegameserver.entities.User;
import net.seanv.stonegameserver.dto.responses.PostGameResult;
import lombok.Getter;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class GameSession {

    private final Player player1,
                         player2;

    private Player choosingPlayer;

    private int chosenNumber,
                wonId,
                ratingChange;

    private TurnType turnType;

    private final GameCallback callback;
    private final long startTime;
    private long endTime;

    private final int id;
    private static final AtomicInteger idCounter = new AtomicInteger();


    public enum TurnType {
        CHOOSING,
        GUESSING;
    }

    private static class Player {
        final User relatedUser;
        final int ratingAtStart;
        int points;

        Player(User relatedUser) {
            this.relatedUser = relatedUser;
            this.ratingAtStart = relatedUser.getRating();
            this.points = 10;
        }

        int getId() { return relatedUser.getId(); }
    }


    public GameSession(User user1, User user2, GameCallback callback) {
        player1 = new Player(user1);
        player2 = new Player(user2);
        choosingPlayer = player1;
        turnType = TurnType.CHOOSING;
        wonId = -1;
        startTime = System.currentTimeMillis();
        this.callback = callback;
        id = idCounter.getAndIncrement();
    }


    public synchronized GameResponse makeChoosingTurn(int id, int amount) {
        if (isOver())                         { return new GameResponse(false, "Game ended, "
                                                            + (id == wonId ? "you won" : "you lose"), id, this); }
        if (id != choosingPlayer.getId()
            || turnType != TurnType.CHOOSING) { return new GameResponse(false, "Not your choosing turn", id, this); }
        if (amount <= 0)                      { return new GameResponse(false, "Chosen less or equal to zero", id, this); }
        if (amount > choosingPlayer.points)   { return new GameResponse(false, "Chosen more than have", id, this); }

        chosenNumber = amount;
        changeTurnType(TurnType.GUESSING);

        return new GameResponse(id, this);
    }

    public synchronized GameResponse makeGuessingTurn(int id, boolean even) {
        if (isOver())                         { return new GameResponse(false, "Game ended, "
                                                            + (id == wonId ? "you won" : "you lose"), id, this); }
        if (id != getGuessingPlayer().getId()
            || turnType != TurnType.GUESSING) { return new GameResponse(false, "Not your guessing turn", id, this); }

        boolean guessed = (even == (chosenNumber%2 == 0));
        if (guessed) {
            choosingPlayer.points      -= chosenNumber;
            getGuessingPlayer().points += chosenNumber;
        } else {
            int change = Math.min(chosenNumber, getGuessingPlayer().points);
            choosingPlayer.points      += change;
            getGuessingPlayer().points -= change;
        }

        choosingPlayer = getGuessingPlayer();
        changeTurnType(TurnType.CHOOSING);

        checkIfGameOver();
        return new GameResponse(id, this);
    }

    private synchronized void changeTurnType(TurnType type) {
        turnType = type;
        callback.onTurnChange(this);
    }

    private synchronized void checkIfGameOver() {
        if (player1.points <= 0) {
            wonId = player2.getId();
            endSession();
        } else if (player2.points <= 0) {
            wonId = player1.getId();
            endSession();
        }
    }

    public synchronized PostGameResult leave(int id) {
        if (!isOver()) {
            wonId = getOpponentPlayerForId(id).getId();
            endSession();
        }
        Player player =  getPlayerById(id);
        return new PostGameResult(id == wonId, player.relatedUser.getRating(), player.ratingAtStart);
    }

    private synchronized void endSession() {
        if (endTime != 0) {
            throw new IllegalStateException("ending session second time");
        }
        ratingChange = callback.onSessionEnd(this);
        endTime = System.currentTimeMillis();
    }

    private synchronized Player getGuessingPlayer() {
        return choosingPlayer == player1 ? player2 : player1;
    }

    public synchronized User getWinner() {
        if (wonId == -1) { throw new IllegalStateException("there is no winner yet"); }
        return getPlayerById(wonId).relatedUser;
    }

    public synchronized User getLoser() {
        if (wonId == -1) { throw new IllegalStateException("there is no loser yet"); }
        return getOpponentPlayerForId(wonId).relatedUser;
    }

    public User[] getUsers() {
        return new User[] { player1.relatedUser, player2.relatedUser };
    }

    public synchronized User getTurningUser() {
        return getTurningPlayer().relatedUser;
    }

    public synchronized boolean isMyTurn(int id) {
        if (isOver()) { return false; }
        return getTurningPlayer().getId() == id;
    }

    public synchronized int getPointsById(int id) {
        return getPlayerById(id).points;
    }

    public User getOpponent(int id) {
        return getOpponentPlayerForId(id).relatedUser;
    }

    private Player getPlayerById(int id) {
        if (id == player1.getId()) {
            return player1;
        } else if (id == player2.getId()) {
            return player2;
        } else {
            throw new IllegalArgumentException("id " + id);
        }
    }

    private Player getOpponentPlayerForId(int id) {
        if (id == player1.getId()) {
            return player2;
        } else if (id == player2.getId()) {
            return player1;
        } else {
            throw new IllegalArgumentException("id " + id);
        }
    }

    private synchronized Player getTurningPlayer() {
        return switch (turnType) {
            case CHOOSING -> choosingPlayer;
            case GUESSING -> getGuessingPlayer();
        };
    }

    public synchronized boolean isOver() {
        return endTime != 0;
    }

}
