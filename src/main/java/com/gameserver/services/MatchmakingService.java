package com.gameserver.services;

import com.gameserver.entities.User;
import com.gameserver.entities.responses.Response;
import com.gameserver.entities.responses.Status;
import com.gameserver.game.Acceptance;
import com.gameserver.game.GameQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class MatchmakingService {

    private final GameQueue queue;
    private final Map<User,Acceptance> userAcceptanceMap;
    private final GameService gameService;

    @Value("${NOTIFYING_TIMEOUT}")
    private long NOTIFYING_TIMEOUT;

    @Autowired
    public MatchmakingService(GameQueue queue, GameService gameService) {
        this.queue = queue;
        this.userAcceptanceMap = Collections.synchronizedMap(new HashMap<>());
        this.gameService = gameService;

        Thread matchmakingThread = new Thread( () -> {
            while(true) {
                if(queue.size() >= 2) {
                    User[] users = queue.pollTwo();
                    createAcceptance(users[0], users[1]);
                }
                try { Thread.sleep(200); } catch (InterruptedException e) { throw new RuntimeException(e); }
            }
        });
        matchmakingThread.start();
    }


    public Response addInQueue(User user){
        if(isInQueue(user)) { return Response.ALREADY_IN_QUEUE; }
        if(isInAcceptance(user)) { return Response.ALREADY_IN_ACCEPTANCE; }
        if(gameService.isInGame(user)) { return Response.ALREADY_IN_GAME; }
        queue.add(user);
        return Response.OK;
    }

    public boolean leaveQueue(User user) {
        return queue.remove(user);
    }

    public boolean isInQueue(User user) { return queue.contains(user); }


    private void createAcceptance(User user1, User user2) {
        Acceptance acceptance = new Acceptance(user1, user2, this::deleteAcceptance);
        userAcceptanceMap.put(user1, acceptance);
        userAcceptanceMap.put(user2, acceptance);
    }

    private void deleteAcceptance(Acceptance acceptance) {
        userAcceptanceMap.remove(acceptance.getUser1());
        userAcceptanceMap.remove(acceptance.getUser2());
    }

    public boolean isInAcceptance(User user) { return userAcceptanceMap.containsKey(user); }

    public boolean acceptGame(User user) {
        Acceptance acceptance = userAcceptanceMap.get(user);
        if(acceptance==null) { return false; }
        acceptance.accept(user);

        if(acceptance.isEverybodyAccepted()) {
            gameService.createGameSession(acceptance.getUser1(),acceptance.getUser2());
            deleteAcceptance(acceptance);
        }
        return true;
    }

    public boolean declineGame(User user) {
        Acceptance acceptance = userAcceptanceMap.get(user);
        if(acceptance==null) { return false; }

        acceptance.decline(user); // ?
        deleteAcceptance(acceptance);
        return true;
    }

    public void updateTime(User user) { queue.updateTime(user); }

    public Status getStatus(User user) {
        updateTime(user);
        return new Status(isInQueue(user), isInAcceptance(user), gameService.isInGame(user));
    }


    public DeferredResult<Response> waitUntilGameFound(User user) {
        DeferredResult<Response> deferredResult = new DeferredResult<>(NOTIFYING_TIMEOUT, new Response(false, "timeout"));
        CompletableFuture.runAsync( () -> {
            while(isInQueue(user)) {
                try { Thread.sleep(500); } catch(Exception e) { throw new RuntimeException(e); }
            }
            deferredResult.setResult(new Response(gameService.isInGame(user)));
        });
        return deferredResult;
    }

    public DeferredResult<Response> waitWhileInAcceptance(User user) {
        DeferredResult<Response> deferredResult = new DeferredResult<>(NOTIFYING_TIMEOUT, new Response(false, "timeout"));
        CompletableFuture.runAsync( () -> {
            while(isInAcceptance(user)) {
                try { Thread.sleep(500); } catch(Exception e) { throw new RuntimeException(e); }
            }
            deferredResult.setResult(new Response(gameService.isInGame(user)));
        });
        return deferredResult;
    }

}
