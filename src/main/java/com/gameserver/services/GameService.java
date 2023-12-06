package com.gameserver.services;

import com.gameserver.game.GameQueue;
import com.gameserver.game.GameSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class GameService {

    private final GameQueue queue;
    private final Map<Integer,GameSession> playerGameMap;

    @Autowired
    public GameService(GameQueue queue){
        this.queue = queue;
        this.playerGameMap = new HashMap<>();

        new Thread( () -> {
            synchronized(this) {
                while(true) {
                    if (queue.size() >= 2) {
                        int[] players = queue.pollTwo();
                        GameSession newGameSession = new GameSession(players[0], players[1]);
                        playerGameMap.put(players[0], newGameSession);
                        playerGameMap.put(players[1], newGameSession);
                    }
                    try { wait(100); } catch (InterruptedException e) { throw new RuntimeException(e); }
                }
            }
        }).start();
    }


    public boolean add(int id){
        if(!queue.contains(id) && !playerGameMap.containsKey(id)) {
            queue.add(id);
            return true;
        }
        return false;
    }

    public int peek() { return queue.peek(); }

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

    public void updateTime(int id) {
        queue.updateTime(id);
    }

    public boolean isInQueue(int id) {
        return queue.contains(id);
    }

    public boolean isInGame(int id) {
        return playerGameMap.containsKey(id);
    }


    public String makeChoose(int id, int amount) {
        GameSession game = playerGameMap.get(id);
        return game!=null? game.makeMoveChoose(id, amount) : "No game with this id";
    }

    public String makeGuess(int id, boolean even) {
        GameSession game = playerGameMap.get(id);
        return game!=null? game.makeMoveGuess(id, even) : "No game with this id";
    }
}
