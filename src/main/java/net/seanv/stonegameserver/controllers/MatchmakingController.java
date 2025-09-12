package net.seanv.stonegameserver.controllers;

import net.seanv.stonegameserver.dto.responses.Response;
import net.seanv.stonegameserver.dto.responses.UserStatus;
import net.seanv.stonegameserver.security.AuthUserContext;
import net.seanv.stonegameserver.services.MatchmakingService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
public class MatchmakingController {

    private final MatchmakingService matchmakingService;
    private final AuthUserContext auth;


    public MatchmakingController(MatchmakingService matchmakingService, AuthUserContext auth) {
        this.matchmakingService = matchmakingService;
        this.auth = auth;
    }


    @PostMapping("/findGame")
    public Response findGame() {
        return matchmakingService.addInQueue(auth.loadUser());
    }

    @PostMapping("/leaveQueue")
    public Response leaveQueue() {
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

    @PostMapping("/userStatus")
    public UserStatus userStatus() {
        return matchmakingService.getUserStatus(auth.getId());
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
