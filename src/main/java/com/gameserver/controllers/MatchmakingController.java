package com.gameserver.controllers;

import com.gameserver.entities.responses.Response;
import com.gameserver.entities.responses.Status;
import com.gameserver.security.MyUserDetails;
import com.gameserver.services.GameService;
import com.gameserver.services.MatchmakingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import java.util.concurrent.CompletableFuture;
import com.gameserver.entities.User;

@RestController
public class MatchmakingController {

    private final GameService gameService;
    private final MatchmakingService matchmakingService;

    @Autowired
    public MatchmakingController(GameService gameService, MatchmakingService matchmakingService) {
        this.gameService = gameService;
        this.matchmakingService = matchmakingService;
    }


    @PostMapping("/find")
    public Response find() {
        return matchmakingService.addInQueue(getUser());
    }

    @PostMapping("/leaveQueue")
    public Response leave() {
        return matchmakingService.leaveQueue(getUser()) ? Response.OK : new Response(false, "not in queue");
    }

    @PostMapping("/accept")
    public Response accept() {
        return matchmakingService.acceptGame(getUser()) ? Response.OK : new Response(false, "not in acceptance");
    }

    @PostMapping("/decline")
    public Response decline() {
        return matchmakingService.declineGame(getUser()) ? Response.OK : new Response(false, "not in acceptance");
    }

    @PostMapping("/queue")
    public String print() {
        return matchmakingService.getQueueToString();
    }

    @PostMapping("/status")
    public Status status() {
        return matchmakingService.getStatus(getUser());
    }

    @PostMapping("/notifyWhenFound")
    public DeferredResult<Response> notifyWhenFound() {
        final User user = getUser();
        DeferredResult<Response> deferredResult = new DeferredResult<>((long)1000 * 60 * 60, new Response(false, "timeout"));
        CompletableFuture.runAsync(()->{
            while(matchmakingService.isInQueue(user)) {
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
