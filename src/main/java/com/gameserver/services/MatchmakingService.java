package com.gameserver.services;

import com.gameserver.entities.User;
import com.gameserver.entities.responses.Response;
import com.gameserver.entities.responses.Status;
import com.gameserver.game.Acceptance;
import com.gameserver.game.GameQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MatchmakingService {

    private final GameQueue queue;
    private final Map<User,Acceptance> acceptanceMap;
    private final GameService gameService;

    @Autowired
    public MatchmakingService(GameQueue queue, GameService gameService) {
        this.queue = queue;
        this.acceptanceMap = new HashMap<>();
        this.gameService = gameService;

        Thread matchmakingThread = new Thread( () -> {
            while(true) {
                if(queue.size() >= 2) {
                    User[] users = queue.pollTwo();
//                    gameService.createGameSession(users[0], users[1]);
                    createAcceptance(users[0], users[1]);

                }
                try { Thread.sleep(200); } catch (InterruptedException e) { throw new RuntimeException(e); }
            }
        });
        matchmakingThread.start();
    }


    public Response addInQueue(User user){
        if(isInQueue(user)) {
            return new Response(false, "already in queue");
        }
        if(gameService.isInGame(user)) {
            return new Response(false, "already in game");
        }
        queue.add(user);
        return Response.OK;
    }

    public boolean leaveQueue(User user) {
        return queue.remove(user);
    }

    public boolean isInQueue(User user) { return queue.contains(user); }


    private void createAcceptance(User user1, User user2) {
        Acceptance acceptance = new Acceptance(user1, user2, this::deleteAcceptance);
        acceptanceMap.put(user1, acceptance);
        acceptanceMap.put(user2, acceptance);
    }

    private void deleteAcceptance(Acceptance acceptance) {
        acceptanceMap.remove(acceptance.getUser1());
        acceptanceMap.remove(acceptance.getUser2());
    }

    public boolean isInAcceptance(User user) { return acceptanceMap.containsKey(user); }

    public boolean acceptGame(User user) {
        Acceptance acceptance = acceptanceMap.get(user);
        if(acceptance==null) { return false; }
        acceptance.accept(user);

        if(acceptance.isEverybodyAccepted()) {
            gameService.createGameSession(acceptance.getUser1(),acceptance.getUser2());
            deleteAcceptance(acceptance);
        }
        return true;
    }

    public boolean declineGame(User user) {
        Acceptance acceptance = acceptanceMap.get(user);
        if(acceptance==null) { return false; }

        acceptance.decline(user); // ?
        deleteAcceptance(acceptance);
        return true;
    }


    public void updateTime(User user) { queue.updateTime(user); }

    public String getQueueToString() { return queue.toString(); }

    public Status getStatus(User user) {
        updateTime(user);
        return new Status(isInQueue(user), isInAcceptance(user), gameService.isInGame(user));
    }

}
