package com.gameserver.controllers;

import com.gameserver.game.GameSession;
import com.gameserver.responses.GameResponse;
import com.gameserver.services.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
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


    @GetMapping("makeChoose")
    public GameResponse makeMoveChoose(@RequestParam int id, @RequestParam int amount) {
        return gameService.makeMoveChoose(id, amount);
    }

    @GetMapping("makeGuess")
    public GameResponse makeMoveGuess(@RequestParam int id, @RequestParam boolean even) {
        return gameService.makeMoveGuess(id, even);
    }

    @GetMapping("gameStatus")
    public GameResponse status(@RequestParam int id) {
        return gameService.status(id);
    }

    @GetMapping("leaveGame")
    public boolean leave(@RequestParam int id) {
        return gameService.leaveGame(id);
    }

    @GetMapping("/notifyWhenMove")
    public DeferredResult<Boolean> notifyWhenMove(@RequestParam int id) {

        DeferredResult<Boolean> deferredResult = new DeferredResult<>((long)1000 * 60 * 2, false);
        CompletableFuture.runAsync(()->{
            while(!gameService.isMyMove(id)) {
                try { Thread.sleep(100); } catch(Exception e) { throw new RuntimeException(e); }
            }
            deferredResult.setResult(true);
        });
        return deferredResult;
    }
}
