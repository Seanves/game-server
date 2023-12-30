package com.gameserver.services;

import com.gameserver.entities.responses.Opponent;
import com.gameserver.entities.responses.GameResult;
import com.gameserver.game.GameSession;
import com.gameserver.entities.responses.GameResponse;
import com.gameserver.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import com.gameserver.entities.User;

@Service
public class GameService {

    private final Map<User,GameSession> userGameMap;
    private final UserRepository userRepository;

    @Autowired
    public GameService(UserRepository userRepository){
        this.userGameMap = new HashMap<>();
        this.userRepository = userRepository;

        Thread gameDeletingThread = new Thread( () -> {
            while(true) {
                userGameMap.entrySet().removeIf(entry -> entry.getValue().isOver() && System.currentTimeMillis() > entry.getValue().getEndTime() + 1000 * 60);
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
        return game!=null? game.makeMoveChoose(user.getId(), amount) : GameResponse.NOT_IN_GAME;
    }

    public GameResponse makeMoveGuess(User user, boolean even) {
        GameSession game = userGameMap.get(user);
        return game!=null? game.makeMoveGuess(user.getId(), even) : GameResponse.NOT_IN_GAME;
    }

    public GameResponse getStatus(User user) {
        GameSession game = userGameMap.get(user);
        return game!=null? new GameResponse(user.getId(), game) : GameResponse.NOT_IN_GAME;
    }

    public Opponent getOpponent(User userTo) {
        GameSession game = userGameMap.get(userTo);
        User opponentUser = game.getOpponent(userTo);
        return new Opponent(opponentUser);
    }


    private int onSessionEnd(GameSession game) {
        User winner = game.getWinner();
        User loser = game.getLoser();

        int change = (int) ((float) loser.getRating() / winner.getRating() * 20);
        winner.addRating(change);
        loser.subtractRating(change);

        winner.incrementWins();
        winner.incrementGamesPlayed();
        loser.incrementGamesPlayed();

        userRepository.save(winner);
        userRepository.save(loser);

        return change;
    }
}
