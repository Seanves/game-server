package com.gameserver.services;

import com.gameserver.util.GameQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class GameService {

    private final GameQueue queue;
    private final Map<String,Integer> ipGameMap;
    private int id;

    @Autowired
    public GameService(GameQueue queue){
        this.queue = queue;
        this.ipGameMap = new HashMap<>();
        this.id = 0;

        new Thread( () -> {
            synchronized(this) {
                while(true) {
                    if (queue.size() >= 2) {
                        String[] polled = queue.pollTwo();
                        ipGameMap.put(polled[0], id);
                        ipGameMap.put(polled[1], id);
                        id++;
                    }
                    try { wait(100); } catch (InterruptedException e) { throw new RuntimeException(e); }
                }
            }
        }).start();
    }


    public boolean add(String ip){
        if(!queue.contains(ip) && !ipGameMap.containsKey(ip)) {
            queue.add(ip);
            return true;
        }
        return false;
    }

    public String peek() { return queue.peek(); }

    public int size() { return queue.size(); }

    public void printQueue() {
        System.out.println(queue);
    }

    public String getQueueToString() {
        return queue.toString();
    }

    public String getIpGameMapToString() {
        return ipGameMap.toString();
    }

    public void updateTime(String ip) {
        queue.updateTime(ip);
    }

    public boolean isInQueue(String ip) {
        return queue.contains(ip);
    }

    public boolean isInGame(String ip) {
        return ipGameMap.containsKey(ip);
    }
}
