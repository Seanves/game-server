package com.gameserver.controllers;

import com.gameserver.entities.User;
import com.gameserver.entities.responses.GameResponse;
import com.gameserver.entities.responses.Opponent;
import com.gameserver.entities.responses.GameResult;
import com.gameserver.entities.responses.Response;
import com.gameserver.security.MyUserDetails;
import com.gameserver.services.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.CompletableFuture;

@RestController
public class GameController {

    private final GameService gameService;

    @Autowired
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }


    @PostMapping("/makeChoose")
    public GameResponse makeMoveChoose(@RequestBody int amount) { return gameService.makeMoveChoose(getUser(), amount); }

    @PostMapping("/makeGuess")
    public GameResponse makeMoveGuess(@RequestBody boolean even) {
        return gameService.makeMoveGuess(getUser(), even);
    }

    @PostMapping("/gameStatus")
    public GameResponse status() {
        return gameService.getStatus(getUser());
    }

    @PostMapping("/opponent")
    public Opponent opponent() {
        return gameService.getOpponent(getUser());
    }

    @PostMapping("/leaveGame")
    public GameResult leave() { return gameService.leaveGame(getUser()); }

//    @PostMapping("/ratingChanges")
//    public GameResult ratingChanges() { return gameService.getRatingChanges(getUser()); }

    @PostMapping("/notifyWhenMove")
    public DeferredResult<Response> notifyWhenMove() {
        final User user = getUser();
        DeferredResult<Response> deferredResult = new DeferredResult<>((long)1000 * 60 * 2, new Response(false, "timeout"));
        CompletableFuture.runAsync(()->{
            while(!gameService.isMyMove(user)) {
                try { Thread.sleep(100); } catch(Exception e) { throw new RuntimeException(e); }
            }
            deferredResult.setResult(new Response(gameService.isMyMove(user)));
        });
        return deferredResult;
    }

    @PostMapping("/games")
    public String games() {
        return gameService.getPlayerGameMapToString();
    }


    private User getUser() {
        return ((MyUserDetails)(SecurityContextHolder.getContext().getAuthentication().getPrincipal())).getUser();
    }
}
