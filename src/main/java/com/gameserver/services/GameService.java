package com.gameserver.services;

import com.gameserver.game.GameQueue;
import com.gameserver.game.GameSession;
import com.gameserver.entities.requests.ChooseMove;
import com.gameserver.entities.requests.GuessMove;
import com.gameserver.entities.responses.GameResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import com.gameserver.entities.User;

@Service
public class GameService {

    private final GameQueue queue;
    private final Map<User,GameSession> playerGameMap;

    @Autowired
    public GameService(GameQueue queue){
        this.queue = queue;
        this.playerGameMap = new HashMap<>();

        new Thread( () -> {
            synchronized(this) {
                while(true) {
                    if (queue.size() >= 2) {
                        User[] users = queue.pollTwo();
                        GameSession newGameSession = new GameSession(users[0], users[1]);
                        playerGameMap.put(users[0], newGameSession);
                        playerGameMap.put(users[1], newGameSession);
                    }
                    try { wait(100); } catch (InterruptedException e) { throw new RuntimeException(e); }
                }
            }
        }).start();
    }


    public void add(User user){
        queue.add(user);
    }

    public boolean leaveQueue(User user) {
        return queue.remove(user);
    }

    public boolean leaveGame(User user) {
        return playerGameMap.remove(user) !=null;
    }

    public User peek() { return queue.peek(); }

    public int size() { return queue.size(); }

    public void printQueue() {
        System.out.println(queue);
    }

    public String getQueueToString() {
        return queue.toString();
    }

    public String getPlayerGameMapToString() {
        return playerGameMap.toString();
    }

    public void updateTime(User user) {
        int id = user.getId();
        queue.updateTime(user);
    }

    public boolean isInQueue(User user) {
        return queue.contains(user);
    }

    public boolean isInGame(User user) {
        return playerGameMap.containsKey(user);
    }

    public boolean isMyMove(User user) {
        GameSession game = playerGameMap.get(user);
        return game.getStage()==game.CHOOSING && user.getId()==game.getChoosingPlayer() ||
               game.getStage()==game.GUESSING && user.getId()==game.getGuessingPlayer();
    }


    public GameResponse makeMoveChoose(User user, int amount) {
        GameSession game = playerGameMap.get(user);
        return game!=null? game.makeMoveChoose(user.getId(),amount) : GameResponse.NO_GAME;
    }

    public GameResponse makeMoveGuess(User user, boolean even) {
        GameSession game = playerGameMap.get(user);
        return game!=null? game.makeMoveGuess(user.getId(), even) : GameResponse.NO_GAME;
    }

    public GameResponse status(User user) {
        GameSession game = playerGameMap.get(user);
        return game!=null? new GameResponse(user.getId(), game) : GameResponse.NO_GAME;
    }
}
