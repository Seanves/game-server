package net.seanv.stonegameserver.services;

import net.seanv.stonegameserver.dto.responses.GameResponse;
import net.seanv.stonegameserver.dto.responses.Opponent;
import net.seanv.stonegameserver.dto.responses.PostGameResult;
import net.seanv.stonegameserver.dto.responses.Response;
import net.seanv.stonegameserver.entities.User;
import net.seanv.stonegameserver.game.GameSession;
import net.seanv.stonegameserver.game.GameCallback;
import net.seanv.stonegameserver.util.DeferredResultsHolder;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {

    private final Map<Integer, GameSession> userIdGameMap;
    private final GameCallback callback;
    private final DeferredResultsHolder<Response> waitingTurnChangeResults;
    private final DeferredResultsHolder<Response> waitingOwnTurnResults;

    private final long ENDED_SESSION_TIMEOUT = 1000 * 60,
                       FORCED_SESSION_TIMEOUT = 1000 * 60 * 60 * 2; // 2 hours
    private final int TIMEOUT_FREQUENCY = 1000 * 30;


    public GameService(GameSessionEndService sessionEndService) {
        this.userIdGameMap = new ConcurrentHashMap<>();
        this.waitingTurnChangeResults = new DeferredResultsHolder<>(Response.TIMEOUT);
        this.waitingOwnTurnResults = new DeferredResultsHolder<>(Response.TIMEOUT);
        this.callback = new GameCallback(
                game -> {
                    cancelWaitingTurns(game);
                    return sessionEndService.onSessionEnd(game);
                },
                this::onTurnChange
        );

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
        GameSession newGameSession = new GameSession(user1, user2, callback);
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

    private void onTurnChange(GameSession game) {
        for (User user : game.getUsers()) {
            waitingTurnChangeResults.complete(user.getId(), Response.OK);
        }

        int turningUserId = game.getTurningUser().getId();
        waitingOwnTurnResults.complete(turningUserId, Response.OK);
    }

    private void cancelWaitingTurns(GameSession game) {
        for (User user : game.getUsers()) {
            waitingOwnTurnResults.complete(user.getId(), new Response(false));
            waitingTurnChangeResults.complete(user.getId(), new Response(false));
        }
    }


    public DeferredResult<Response> waitOwnTurn(int userId) {
        GameSession game = userIdGameMap.get(userId);

        if (game == null || game.isOver()) {
            return waitingOwnTurnResults.createAndSet(new Response(false));
        }
        if (game.isMyTurn(userId)) {
            return waitingOwnTurnResults.createAndSet(new Response(true));
        }

        return waitingOwnTurnResults.tryPut(userId, new Response(false));
    }

    public DeferredResult<Response> waitTurnChange(int userId) {
        GameSession game = userIdGameMap.get(userId);

        if (game == null || game.isOver()) {
            return waitingTurnChangeResults.createAndSet(new Response(false));
        }

        return waitingTurnChangeResults.tryPut(userId, new Response(false));
    }
}
