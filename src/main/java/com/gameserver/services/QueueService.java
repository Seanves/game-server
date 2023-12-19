package com.gameserver.services;

import com.gameserver.entities.User;
import com.gameserver.game.GameQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QueueService {

    private final GameQueue queue;

    @Autowired
    public QueueService(GameQueue queue, GameService gameService) {
        this.queue = queue;

        Thread matchmakingThread = new Thread( () -> {
            while(true) {
                if(queue.size() >= 2) {
                    User[] users = queue.pollTwo();
                    gameService.createGameSession(users[0], users[1]);
                }
                try { Thread.sleep(200); } catch (InterruptedException e) { throw new RuntimeException(e); }
            }
        });
        matchmakingThread.start();
    }


    public void add(User user){
        queue.add(user);
    }

    public boolean leaveQueue(User user) {
        return queue.remove(user);
    }

    public void updateTime(User user) { queue.updateTime(user); }

    public boolean isInQueue(User user) { return queue.contains(user); }

    public String getQueueToString() { return queue.toString(); }

}
