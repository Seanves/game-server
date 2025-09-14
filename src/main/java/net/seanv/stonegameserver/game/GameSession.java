package net.seanv.stonegameserver.game;

import net.seanv.stonegameserver.dto.responses.GameResponse;
import net.seanv.stonegameserver.entities.User;
import net.seanv.stonegameserver.dto.responses.PostGameResult;
import lombok.Getter;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@Getter
public class GameSession {

    private final Player player1,
                         player2;

    private Player choosingPlayer;

    private int chosenNumber,
                wonId,
                ratingChange;

    private TurnType turnType;

    private final Function<GameSession,Integer> onSessionEndCallback;
    private final long startTime;
    private long endTime;

    private final int id;
    private static final AtomicInteger idCounter = new AtomicInteger();


    public enum TurnType {
        CHOOSING,
        GUESSING;
    }

    private static class Player {
        User relatedUser;
        int points;
        int ratingAtStart;

        Player(User relatedUser) {
            this.relatedUser = relatedUser;
            this.points = 10;
            this.ratingAtStart = relatedUser.getRating();
        }

        int getId() { return relatedUser.getId(); }
    }


    public GameSession(User user1, User user2, Function<GameSession,Integer> onSessionEndCallback) {
        player1 = new Player(user1);
        player2 = new Player(user2);
        choosingPlayer = player1;
        turnType = TurnType.CHOOSING;
        wonId = -1;
        startTime = System.currentTimeMillis();
        this.onSessionEndCallback = onSessionEndCallback;
        id = idCounter.getAndIncrement();
    }


    public GameResponse makeChoosingTurn(int id, int amount) {
        if (isOver())                         { return new GameResponse(false, "Game ended, " + (id==wonId ? "you won" : "you lose"), id, this); }
        if (id != choosingPlayer.getId()
            || turnType != TurnType.CHOOSING) { return new GameResponse(false, "Not your choosing turn", id, this); }
        if (amount <= 0)                      { return new GameResponse(false, "Chosen less or equal to zero", id, this); }
        if (amount > choosingPlayer.points)   { return new GameResponse(false, "Chosen more than have", id, this); }

        chosenNumber = amount;
        turnType = TurnType.GUESSING;

        return new GameResponse(id, this);
    }

    public GameResponse makeGuessingTurn(int id, boolean even) {
        if (isOver())                         { return new GameResponse(false, "Game ended, " + (id== wonId ? "you won" : "you lose"), id, this); }
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
        turnType = TurnType.CHOOSING;

        checkIfGameOver();
        return new GameResponse(id, this);
    }


    private void checkIfGameOver() {
        if (player1.points <= 0) {
            wonId = player2.getId();
            endSession();
        } else if (player2.points <= 0) {
            wonId = player1.getId();
            endSession();
        }
    }

    public PostGameResult leave(int id) {
        if (!isOver()) {
            wonId = getOpponentPlayerForId(id).getId();
            endSession();
        }
        Player player =  getPlayerById(id);
        return new PostGameResult(id == wonId, player.relatedUser.getRating(), player.ratingAtStart);
    }

    private void endSession() {
        if (endTime != 0) {
            throw new IllegalStateException("ending session second time");
        }
        ratingChange = onSessionEndCallback.apply(this);
        endTime = System.currentTimeMillis();
    }

    private Player getGuessingPlayer() {
        return choosingPlayer == player1 ? player2 : player1;
    }

    public User getWinner() {
        if (wonId == -1) { throw new IllegalStateException("there is no winner yet"); }
        return getPlayerById(wonId).relatedUser;
    }

    public User getLoser() {
        if (wonId == -1) { throw new IllegalStateException("there is no loser yet"); }
        return getOpponentPlayerForId(wonId).relatedUser;
    }

    public boolean isMyTurn(int id) {
        if (isOver()) { return false; }

        return turnType == TurnType.CHOOSING && id == choosingPlayer.getId()
            || turnType == TurnType.GUESSING && id == getGuessingPlayer().getId();

    }

    public int getPointsById(int id) {
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

    public boolean isOver() {
        return endTime != 0;
    }

    @Override
    public String toString() {
        return "GameSession{" + "\n  " +
                "player1.points=" + player1.points + ",\n  " +
                "player2.points=" + player2.points + ",\n  " +
                "choosingPlayer.id=" + choosingPlayer.getId() + ",\n  " +
                "chosenNumber=" + chosenNumber + ",\n  " +
                "turnType=" + turnType + "\n" +
                '}';
    }

}
