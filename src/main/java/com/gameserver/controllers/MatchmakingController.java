package com.gameserver.controllers;

import com.gameserver.entities.responses.Response;
import com.gameserver.entities.responses.Status;
import com.gameserver.security.MyUserDetails;
import com.gameserver.services.MatchmakingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import com.gameserver.entities.User;

@RestController
public class MatchmakingController {

    private final MatchmakingService matchmakingService;

    @Autowired
    public MatchmakingController(MatchmakingService matchmakingService) {
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
        return matchmakingService.waitUntilGameFound(getUser());
    }

    @PostMapping("/notifyWhenNotInAcceptance")
    public DeferredResult<Response> notifyWhenNotInAcceptance() {
        return matchmakingService.waitWhileInAcceptance(getUser());
    }


    private User getUser() {
        return ((MyUserDetails)(SecurityContextHolder.getContext().getAuthentication().getPrincipal())).getUser();
    }
}
