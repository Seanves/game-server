package com.gameserver.services;

import com.gameserver.game.GameQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class GameService {

    private final GameQueue queue;
    private final Map<Integer,Integer> playerGameMap;
    private int id;

    @Autowired
    public GameService(GameQueue queue){
        this.queue = queue;
        this.playerGameMap = new HashMap<>();
        this.id = 0;

        new Thread( () -> {
            synchronized(this) {
                while(true) {
                    if (queue.size() >= 2) {
                        int[] polled = queue.pollTwo();
                        playerGameMap.put(polled[0], id);
                        playerGameMap.put(polled[1], id);
                        id++;
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
}
