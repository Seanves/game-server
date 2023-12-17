package com.gameserver.controllers;

import com.gameserver.entities.responses.Response;
import com.gameserver.entities.responses.Status;
import com.gameserver.security.MyUserDetails;
import com.gameserver.services.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import java.util.concurrent.CompletableFuture;
import com.gameserver.entities.User;

@RestController
public class QueueController {

    private final GameService gameService;

    @Autowired
    public QueueController(GameService gameService) {
        this.gameService = gameService;
    }


    @PostMapping("/find")
    public Response find() {
        User user = getUser();
        if(gameService.isInQueue(user)) {
            return new Response(false, "already in queue");
        }
        else if(gameService.isInGame(user)) {
            return new Response(false, "already in game");
        }
        else {
            gameService.add(user);
            return Response.OK;
        }
    }

    @PostMapping("/leaveQueue")
    public Response leave() {
        return gameService.leaveQueue(getUser()) ? Response.OK : new Response(false, "not in queue");
    }

    @PostMapping("/queue")
    public String print() {
        return gameService.getQueueToString();
    }

    @PostMapping("/status")
    public Status status() {
        User user = getUser();
        gameService.updateTime(user);
        return new Status(gameService.isInQueue(user), gameService.isInGame(user));
    }

    @PostMapping("/notifyWhenFound")
    public DeferredResult<Response> notifyWhenFound() {
        final User user = getUser();
        DeferredResult<Response> deferredResult = new DeferredResult<>((long)1000 * 60 * 60, new Response(false, "timeout"));
        CompletableFuture.runAsync(()->{
            while(gameService.isInQueue(user)) {
                try { Thread.sleep(100); } catch(Exception e) { throw new RuntimeException(e); }
            }
            deferredResult.setResult(new Response(gameService.isInGame(user)));
        });
        return deferredResult;
    }


//    private int idCounter = 0;
//
//    private synchronized int nextId() {
//        return ++idCounter;
//    }


    private User getUser() {
        return ((MyUserDetails)(SecurityContextHolder.getContext().getAuthentication().getPrincipal())).getUser();
    }

//    private int getId() {
//        return jwtManager.validateAndGetId(
//                ((ServletRequestAttributes) RequestContextHolder
//                        .getRequestAttributes())
//                        .getRequest().getHeader("Authorization")
//                        .substring(7));
//    }
}
