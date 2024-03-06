package com.gameserver.controllers;

import com.gameserver.entities.responses.Response;
import com.gameserver.entities.responses.Status;
import com.gameserver.security.AuthenticatedUserContext;
import com.gameserver.services.MatchmakingService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
public class MatchmakingController {

    private final MatchmakingService matchmakingService;
    private final AuthenticatedUserContext auth;


    public MatchmakingController(MatchmakingService matchmakingService, AuthenticatedUserContext auth) {
        this.matchmakingService = matchmakingService;
        this.auth = auth;
    }


    @PostMapping("/findGame")
    public Response find() {
        return matchmakingService.addInQueue(auth.getId());
    }

    @PostMapping("/leaveQueue")
    public Response leave() {
        return matchmakingService.leaveQueue(auth.getId()) ? Response.OK : Response.NOT_IN_QUEUE;
    }

    @PostMapping("/accept")
    public Response accept() {
        return matchmakingService.acceptGame(auth.getId()) ? Response.OK : Response.NOT_IN_ACCEPTANCE;
    }

    @PostMapping("/decline")
    public Response decline() {
        return matchmakingService.declineGame(auth.getId()) ? Response.OK : Response.NOT_IN_ACCEPTANCE;
    }

    @PostMapping("/status")
    public Status status() {
        return matchmakingService.getStatus(auth.getId());
    }


    @PostMapping("/notifyWhenFound")
    public DeferredResult<Response> notifyWhenFound() {
        return matchmakingService.waitUntilGameFound(auth.getId());
    }

    @PostMapping("/notifyWhenNotInAcceptance")
    public DeferredResult<Response> notifyWhenNotInAcceptance() {
        return matchmakingService.waitWhileInAcceptance(auth.getId());
    }


}
