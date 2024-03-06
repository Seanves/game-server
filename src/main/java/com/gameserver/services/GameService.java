package com.gameserver.services;

import com.gameserver.entities.GameResult;
import com.gameserver.entities.responses.Opponent;
import com.gameserver.entities.responses.PostGameResult;
import com.gameserver.entities.responses.Response;
import com.gameserver.game.GameSession;
import com.gameserver.entities.responses.GameResponse;
import com.gameserver.repositories.GameResultRepository;
import com.gameserver.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import com.gameserver.entities.User;
import org.springframework.web.context.request.async.DeferredResult;

@Service
public class GameService {

    private final Map<Integer,GameSession> userIdGameMap;
    private final UserRepository userRepository;
    private final GameResultRepository gameResultRepository;

    private final long ENDED_SESSION_TIMEOUT = 1000 * 60,
                       FORCED_SESSION_TIMEOUT = 1000 * 60 * 60 * 2; // 2 hours

    @Value("${NOTIFYING_TIMEOUT}")
    private long NOTIFYING_TIMEOUT;
    private final int TIMEOUT_FREQUENCY = 1000 * 30;


    public GameService(UserRepository userRepository, GameResultRepository gameResultRepository){
        this.userIdGameMap = Collections.synchronizedMap( new HashMap<>() );
        this.userRepository = userRepository;
        this.gameResultRepository = gameResultRepository;

        Thread gameTimeoutingThread = new Thread( () -> {
            while(true) {
                userIdGameMap.entrySet().removeIf(
                        entry -> {
                                    long startTime = entry.getValue().getStartTime(),
                                         endTime = entry.getValue().getEndTime(),
                                         currentTime = System.currentTimeMillis();

    /* timeout after session end */ return entry.getValue().isOver()
                                    && currentTime > endTime + ENDED_SESSION_TIMEOUT
               /* forced timeout */ || currentTime > startTime + FORCED_SESSION_TIMEOUT;
                        }
                );
                try { Thread.sleep(TIMEOUT_FREQUENCY); } catch (InterruptedException e) { throw new RuntimeException(e); }
            }
        });
        gameTimeoutingThread.start();
    }


    public void createGameSession(User user1, User user2) {
        GameSession newGameSession = new GameSession(user1, user2, this::onSessionEnd);
        userIdGameMap.put(user1.getId(), newGameSession);
        userIdGameMap.put(user2.getId(), newGameSession);
    }

    public PostGameResult leaveGame(int id) {
        GameSession game = userIdGameMap.remove(id);
        if(game==null) { return new PostGameResult(false, -1, -1, -1); }

        return game.leave(id);
    }

    public boolean isInGame(int id) {
        return userIdGameMap.containsKey(id);
    }

    public boolean isMyMove(int id) {
        GameSession game = userIdGameMap.get(id);
        return game.isMyMove(id);
    }


    public GameResponse makeMoveChoose(int id, int amount) {
        GameSession game = userIdGameMap.get(id);
        return game!=null ? game.makeMoveChoose(id, amount) : GameResponse.NOT_IN_GAME;
    }

    public GameResponse makeMoveGuess(int id, boolean even) {
        GameSession game = userIdGameMap.get(id);
        return game!=null ? game.makeMoveGuess(id, even) : GameResponse.NOT_IN_GAME;
    }

    public GameResponse getStatus(int id) {
        GameSession game = userIdGameMap.get(id);
        return game!=null ? new GameResponse(id, game) : GameResponse.NOT_IN_GAME;
    }

    public Opponent getOpponent(int forId) {
        GameSession game = userIdGameMap.get(forId);
        if(game == null) { return null; }
        User opponentUser = game.getOpponent(forId);
        return new Opponent(opponentUser);
    }

    public int getGameId(int userId) {
        GameSession game = userIdGameMap.get(userId);
        return game!=null ? game.getId() : -1;
    }

    private int countRatingChange(User winner, User loser) {
        // multiplier
        double m = (float) Math.max(loser.getRating(), 20) / Math.max(winner.getRating(), 20);
        // multiplier closer to 1 by 80%
        m = m + (1 - m) * 0.8;
        // rating change
        int change = (int)(m * 20);
        // min 10, max 40
        change = Math.min(Math.max(change, 10), 40);

        return change;
    }


    private int onSessionEnd(GameSession game) {
        User winner = game.getWinner();
        User loser = game.getLoser();

        int loserRatingBefore = loser.getRating();

        int ratingChange = countRatingChange(winner, loser);

        winner.addRating(ratingChange);
        loser.subtractRating(ratingChange);

        winner.incrementWins();
        winner.incrementGamesPlayed();
        loser.incrementGamesPlayed();

        GameResult result = new GameResult(winner, loser, ratingChange,
                                        loser.getRating() - loserRatingBefore);
        gameResultRepository.save(result);

        userRepository.save(winner);
        userRepository.save(loser);

        return ratingChange;
    }


    public DeferredResult<Response> waitForMyMove(int userId) {
        GameSession game = userIdGameMap.get(userId);
        if(game == null) { return null; }

        DeferredResult<Response> deferredResult = new DeferredResult<>(NOTIFYING_TIMEOUT, new Response(false, "timeout"));
        CompletableFuture.runAsync( () -> {
            while(!game.isOver() && !game.isMyMove(userId)) {
                try { Thread.sleep(100); } catch(Exception e) { throw new RuntimeException(e); }
            }
            deferredResult.setResult(new Response(isMyMove(userId)));
        });
        return deferredResult;
    }

    public DeferredResult<Response> waitForMoveChange(int userId) {
        if(!isInGame(userId)) { return null; }
        GameResponse oldResponse = getStatus(userId);

        GameSession.MoveType oldMoveType = oldResponse.moveType();
        boolean              oldIsYourMove = oldResponse.yourMove();

        DeferredResult<Response> deferredResult = new DeferredResult<>(NOTIFYING_TIMEOUT, new Response(false, "timeout"));
        CompletableFuture.runAsync( () -> {
            GameResponse response = oldResponse;
            while(!response.gameOver() && response.moveType() == oldMoveType
                                       && response.yourMove() == oldIsYourMove) {
                try { Thread.sleep(500); } catch(Exception e) { throw new RuntimeException(e); }
                response = getStatus(userId);
            }
            deferredResult.setResult(new Response(isMyMove(userId)));
        });
        return deferredResult;
    }
}
