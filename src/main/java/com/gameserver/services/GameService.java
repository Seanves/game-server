package com.gameserver.services;

import com.gameserver.entities.responses.Opponent;
import com.gameserver.entities.responses.GameResult;
import com.gameserver.entities.responses.Response;
import com.gameserver.game.GameSession;
import com.gameserver.entities.responses.GameResponse;
import com.gameserver.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.gameserver.entities.User;
import org.springframework.web.context.request.async.DeferredResult;

@Service
public class GameService {

    private final Map<User,GameSession> userGameMap;
    private final UserRepository userRepository;

    private final long ENDED_SESSION_TIMEOUT = 1000 * 60,
                       FORCED_SESSION_TIMEOUT = 1000 * 60 * 60 * 2; // 2 hours

    @Value("${NOTIFYING_TIMEOUT}")
    private long NOTIFYING_TIMEOUT;

    @Autowired
    public GameService(UserRepository userRepository){
        this.userGameMap = new HashMap<>();
        this.userRepository = userRepository;

        Thread gameDeletingThread = new Thread( () -> {
            while(true) {
                userGameMap.entrySet().removeIf(
                        entry ->
                /* timeout after session end */  entry.getValue().isOver() && System.currentTimeMillis() > entry.getValue().getEndTime() + ENDED_SESSION_TIMEOUT
                           /* forced timeout */  || System.currentTimeMillis() > entry.getValue().getStartTime() + FORCED_SESSION_TIMEOUT
                );
                try { Thread.sleep(1000 * 30); } catch (InterruptedException e) { throw new RuntimeException(e); }
            }
        });
        gameDeletingThread.start();
    }


    public void createGameSession(User user1, User user2) {
        GameSession newGameSession = new GameSession(user1, user2, this::onSessionEnd);
        userGameMap.put(user1, newGameSession);
        userGameMap.put(user2, newGameSession);
    }

    public GameResult leaveGame(User user) {
        GameSession game = userGameMap.remove(user);
        if(game==null) { return new GameResult(false, -1, -1); }

        return game.leave(user.getId());
    }

    public boolean isInGame(User user) {
        return userGameMap.containsKey(user);
    }

    public boolean isMyMove(User user) {
        GameSession game = userGameMap.get(user);
        return game.isMyMove(user.getId());
    }


    public GameResponse makeMoveChoose(User user, int amount) {
        GameSession game = userGameMap.get(user);
        return game!=null ? game.makeMoveChoose(user.getId(), amount) : GameResponse.NOT_IN_GAME;
    }

    public GameResponse makeMoveGuess(User user, boolean even) {
        GameSession game = userGameMap.get(user);
        return game!=null ? game.makeMoveGuess(user.getId(), even) : GameResponse.NOT_IN_GAME;
    }

    public GameResponse getStatus(User user) {
        GameSession game = userGameMap.get(user);
        return game!=null ? new GameResponse(user.getId(), game) : GameResponse.NOT_IN_GAME;
    }

    public Opponent getOpponent(User userTo) {
        GameSession game = userGameMap.get(userTo);
        if(game == null) { return null; }
        User opponentUser = game.getOpponent(userTo);
        return new Opponent(opponentUser);
    }


    private int onSessionEnd(GameSession game) {
        User winner = game.getWinner();
        User loser = game.getLoser();

        // count rating change

        // multiplier
        double m = (float) loser.getRating() / winner.getRating();
        // make multiplier closer to 1 by 80%
        m = m + (1 - m) * 0.8;
        // rating change
        int change = (int)(m * 20);
        // min 10, max 40
        change = Math.min(Math.max(change, 10), 40);

        winner.addRating(change);
        loser.subtractRating(change);

        winner.incrementWins();
        winner.incrementGamesPlayed();
        loser.incrementGamesPlayed();

        userRepository.save(winner);
        userRepository.save(loser);

        return change;
    }


    public DeferredResult<Response> waitForMyMove(User user) {
        GameSession game = userGameMap.get(user);
        if(game == null) { return null; }

        DeferredResult<Response> deferredResult = new DeferredResult<>(NOTIFYING_TIMEOUT, new Response(false, "timeout"));
        CompletableFuture.runAsync( () -> {
            while(!game.isOver() && !game.isMyMove(user.getId())) {
                System.out.println(!isMyMove(user));
                try { Thread.sleep(100); } catch(Exception e) { throw new RuntimeException(e); }
            }
            deferredResult.setResult(new Response(isMyMove(user)));
        });
        return deferredResult;
    }

    public DeferredResult<Response> waitForMoveChange(User user) {
        if(!isInGame(user)) { return null; }
        GameResponse oldResponse = getStatus(user);

        GameSession.MoveType oldMoveType = oldResponse.moveType();
        boolean              oldIsYourMove = oldResponse.yourMove();

        DeferredResult<Response> deferredResult = new DeferredResult<>(NOTIFYING_TIMEOUT, new Response(false, "timeout"));
        CompletableFuture.runAsync( () -> {
            GameResponse response = oldResponse;
            while(!response.gameOver() && response.moveType() == oldMoveType
                                       && response.yourMove() == oldIsYourMove) {
                try { Thread.sleep(500); } catch(Exception e) { throw new RuntimeException(e); }
                response = getStatus(user);
            }
            deferredResult.setResult(new Response(isMyMove(user)));
        });
        return deferredResult;
    }
}
