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


    @PostMapping("/findGame")
    public Response find() {
        return matchmakingService.addInQueue(getUser());
    }

    @PostMapping("/leaveQueue")
    public Response leave() {
        return matchmakingService.leaveQueue(getUser()) ? Response.OK : Response.NOT_IN_QUEUE;
    }

    @PostMapping("/accept")
    public Response accept() {
        return matchmakingService.acceptGame(getUser()) ? Response.OK : Response.NOT_IN_ACCEPTANCE;
    }

    @PostMapping("/decline")
    public Response decline() {
        return matchmakingService.declineGame(getUser()) ? Response.OK : Response.NOT_IN_ACCEPTANCE;
    }

    @PostMapping("/status")
    public Status status() {
        return matchmakingService.getStatus(getUser());
    }

    @PostMapping("/notifyWhenFound")
    public DeferredResult<Response> notifyWhenFound() {
        final User user = getUser();
        DeferredResult<Response> deferredResult = new DeferredResult<>((long)1000 * 60 * 60, new Response(false, "timeout"));
        CompletableFuture.runAsync( () -> {
            while(matchmakingService.isInQueue(user)) {
                try { Thread.sleep(100); } catch(Exception e) { throw new RuntimeException(e); }
            }
            deferredResult.setResult(new Response(gameService.isInGame(user)));
        });
        return deferredResult;
    }


    private User getUser() {
        return ((MyUserDetails)(SecurityContextHolder.getContext().getAuthentication().getPrincipal())).getUser();
    }
}
