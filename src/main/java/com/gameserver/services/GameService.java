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


    public void add(int id){
        queue.add(id);
    }

    public boolean leaveQueue(int id) {
        return queue.remove(id);
    }

    public boolean leaveGame(int id) {
        return playerGameMap.remove(id) !=null;
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

    public boolean isMyMove(int id) {
        GameSession game = playerGameMap.get(id);
        return game.getStage()==game.CHOOSING && id==game.getChoosingPlayer() ||
               game.getStage()==game.GUESSING && id==game.getGuessingPlayer();
    }


    public GameResponse makeMoveChoose(ChooseMove move) {
        GameSession game = playerGameMap.get(move.getId());
        return game!=null? game.makeMoveChoose(move.getId(), move.getAmount()) : GameResponse.NO_GAME;
    }

    public GameResponse makeMoveGuess(GuessMove move) {
        GameSession game = playerGameMap.get(move.getId());
        return game!=null? game.makeMoveGuess(move.getId(), move.isEven()) : GameResponse.NO_GAME;
    }

    public GameResponse status(int id) {
        GameSession game = playerGameMap.get(id);
        return game!=null? new GameResponse(id, game) : GameResponse.NO_GAME;
    }
}
