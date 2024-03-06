package com.gameserver.services;

import com.gameserver.entities.User;
import com.gameserver.entities.responses.Response;
import com.gameserver.entities.responses.Status;
import com.gameserver.game.Acceptance;
import com.gameserver.game.GameQueue;
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
    private final Map<Integer,Acceptance> userIdAcceptanceMap;
    private final GameService gameService;
    private final UserLoader userLoader;

    @Value("${NOTIFYING_TIMEOUT}")
    private long NOTIFYING_TIMEOUT;


    public MatchmakingService(GameQueue queue, GameService gameService, UserLoader userLoader) {
        this.queue = queue;
        this.userLoader = userLoader;
        this.userIdAcceptanceMap = Collections.synchronizedMap(new HashMap<>());
        this.gameService = gameService;
    }


    public Response addInQueue(int id) {
        if(isInQueue(id))            { return Response.ALREADY_IN_QUEUE; }
        if(isInAcceptance(id))       { return Response.ALREADY_IN_ACCEPTANCE; }
        if(gameService.isInGame(id)) { return Response.ALREADY_IN_GAME; }
        queue.add(id);

        if(queue.size() >= 2) {
            int[] ids = queue.pollTwo();
            User user1 = userLoader.load(ids[0]);
            User user2 = userLoader.load(ids[1]);
            createAcceptance(user1, user2);
        }
        return Response.OK;
    }

    public boolean leaveQueue(int id) { return queue.remove(id); }

    public boolean isInQueue(int id) { return queue.contains(id); }

    private void createAcceptance(User user1, User user2) {
        Acceptance acceptance = new Acceptance(user1, user2, this::deleteAcceptance);
        userIdAcceptanceMap.put(user1.getId(), acceptance);
        userIdAcceptanceMap.put(user2.getId(), acceptance);
    }

    private void deleteAcceptance(Acceptance acceptance) {
        userIdAcceptanceMap.remove(acceptance.getUser1().getId());
        userIdAcceptanceMap.remove(acceptance.getUser2().getId());
    }

    public boolean isInAcceptance(int id) { return userIdAcceptanceMap.containsKey(id); }

    public boolean acceptGame(int id) {
        Acceptance acceptance = userIdAcceptanceMap.get(id);
        if(acceptance==null) { return false; }
        acceptance.accept(id);

        if(acceptance.isEverybodyAccepted()) {
            gameService.createGameSession(acceptance.getUser1(),acceptance.getUser2());
            deleteAcceptance(acceptance);
        }
        return true;
    }

    public boolean declineGame(int id) {
        Acceptance acceptance = userIdAcceptanceMap.get(id);
        if(acceptance==null) { return false; }

        acceptance.decline(id); // ?
        deleteAcceptance(acceptance);
        return true;
    }

    public void updateTime(int id) { queue.updateTime(id); }

    public Status getStatus(int id) {
        updateTime(id);
        return new Status(isInQueue(id), isInAcceptance(id), gameService.isInGame(id));
    }


    public DeferredResult<Response> waitUntilGameFound(int userId) {
        DeferredResult<Response> deferredResult = new DeferredResult<>(NOTIFYING_TIMEOUT, new Response(false, "timeout"));
        CompletableFuture.runAsync( () -> {
            while(isInQueue(userId)) {
                try { Thread.sleep(500); } catch(Exception e) { throw new RuntimeException(e); }
            }
            deferredResult.setResult(new Response(gameService.isInGame(userId)));
        });
        return deferredResult;
    }

    public DeferredResult<Response> waitWhileInAcceptance(int userId) {
        DeferredResult<Response> deferredResult = new DeferredResult<>(NOTIFYING_TIMEOUT, new Response(false, "timeout"));
        CompletableFuture.runAsync( () -> {
            while(isInAcceptance(userId)) {
                try { Thread.sleep(500); } catch(Exception e) { throw new RuntimeException(e); }
            }
            deferredResult.setResult(new Response(gameService.isInGame(userId)));
        });
        return deferredResult;
    }

}
