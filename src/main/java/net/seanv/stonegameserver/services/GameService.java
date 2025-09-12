package net.seanv.stonegameserver.services;

import net.seanv.stonegameserver.entities.GameResult;
import net.seanv.stonegameserver.dto.responses.Opponent;
import net.seanv.stonegameserver.dto.responses.PostGameResult;
import net.seanv.stonegameserver.dto.responses.Response;
import net.seanv.stonegameserver.game.GameSession;
import net.seanv.stonegameserver.dto.responses.GameResponse;
import net.seanv.stonegameserver.repositories.GameResultRepository;
import net.seanv.stonegameserver.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import net.seanv.stonegameserver.entities.User;
import org.springframework.web.context.request.async.DeferredResult;

@Service
public class GameService {

    private final Map<Integer,GameSession> userIdGameMap;
    private final UserRepository userRepository;
    private final GameResultRepository gameResultRepository;

    private final long ENDED_SESSION_TIMEOUT = 1000 * 60,
                       FORCED_SESSION_TIMEOUT = 1000 * 60 * 60 * 2; // 2 hours
    private final long NOTIFYING_TIMEOUT;
    private final int TIMEOUT_FREQUENCY = 1000 * 30;


    public GameService(UserRepository userRepository, GameResultRepository gameResultRepository,
                       @Value("${NOTIFYING_TIMEOUT}") long NOTIFYING_TIMEOUT) {
        this.userIdGameMap = new ConcurrentHashMap<>();
        this.userRepository = userRepository;
        this.gameResultRepository = gameResultRepository;
        this.NOTIFYING_TIMEOUT = NOTIFYING_TIMEOUT;

        Thread gameTimeoutingThread = new Thread( () -> {
            while (true) {
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
        if (game == null) { return new PostGameResult(false, -1, -1); }

        return game.leave(id);
    }

    public boolean isInGame(int id) {
        return userIdGameMap.containsKey(id);
    }

    public boolean isMyTurn(int id) {
        GameSession game = userIdGameMap.get(id);
        return game != null && game.isMyTurn(id);
    }


    public GameResponse makeChoosingTurn(int id, int amount) {
        GameSession game = userIdGameMap.get(id);
        return game != null ? game.makeChoosingTurn(id, amount) : GameResponse.NOT_IN_GAME;
    }

    public GameResponse makeGuessingTurn(int id, boolean even) {
        GameSession game = userIdGameMap.get(id);
        return game != null ? game.makeGuessingTurn(id, even) : GameResponse.NOT_IN_GAME;
    }

    public GameResponse getGameStatus(int id) {
        GameSession game = userIdGameMap.get(id);
        return game != null ? new GameResponse(id, game) : GameResponse.NOT_IN_GAME;
    }

    public Opponent getOpponent(int forId) {
        GameSession game = userIdGameMap.get(forId);
        if (game == null) { return null; }
        User opponentUser = game.getOpponent(forId);
        return new Opponent(opponentUser);
    }

    public int getGameId(int userId) {
        GameSession game = userIdGameMap.get(userId);
        return game != null ? game.getId() : -1;
    }

    private int countRatingChange(User winner, User loser) {
        // multiplier
        double m = (double) Math.max(loser.getRating(), 20) / Math.max(winner.getRating(), 20);
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

        int loserRatingChange = loser.getRating() - loserRatingBefore;

        winner.incrementWins();
        winner.incrementGamesPlayed();
        loser.incrementGamesPlayed();

        GameResult result = new GameResult(winner, loser, ratingChange, loserRatingChange);
        gameResultRepository.save(result);

        userRepository.save(winner);
        userRepository.save(loser);

        return ratingChange;
    }


    public DeferredResult<Response> waitForMyTurn(int userId) {
        GameSession game = userIdGameMap.get(userId);
        if (game == null) { return null; }

        DeferredResult<Response> deferredResult = new DeferredResult<>(NOTIFYING_TIMEOUT, new Response(false, "timeout"));
        CompletableFuture.runAsync( () -> {
            while (!game.isOver() && !game.isMyTurn(userId)) {
                try { Thread.sleep(100); } catch (Exception e) { throw new RuntimeException(e); }
            }
            deferredResult.setResult(new Response(isMyTurn(userId)));
        });
        return deferredResult;
    }

    public DeferredResult<Response> waitForTurnChange(int userId) {
        if (!isInGame(userId)) { return null; }
        GameResponse oldResponse = getGameStatus(userId);

        GameSession.TurnType oldTurnType = oldResponse.turnType();
        boolean              oldIsYourTurn = oldResponse.yourTurn();

        DeferredResult<Response> deferredResult = new DeferredResult<>(NOTIFYING_TIMEOUT, new Response(false, "timeout"));
        CompletableFuture.runAsync( () -> {
            GameResponse response = oldResponse;
            while (!response.gameOver() && response.turnType() == oldTurnType
                                        && response.yourTurn() == oldIsYourTurn) {
                try { Thread.sleep(500); } catch (Exception e) { throw new RuntimeException(e); }
                response = getGameStatus(userId);
            }
            deferredResult.setResult(new Response(isMyTurn(userId)));
        });
        return deferredResult;
    }
}
