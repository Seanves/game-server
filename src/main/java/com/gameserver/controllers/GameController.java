package com.gameserver.controllers;

import com.gameserver.entities.requests.ChooseMove;
import com.gameserver.entities.requests.GuessMove;
import com.gameserver.entities.responses.GameResponse;
import com.gameserver.services.GameService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public GameResponse makeMoveChoose(@RequestBody ChooseMove move) {
        return gameService.makeMoveChoose(move);
    }

    @PostMapping("/makeGuess")
    public GameResponse makeMoveGuess(@RequestBody GuessMove move) {
        return gameService.makeMoveGuess(move);
    }

    @PostMapping("/gameStatus")
    public GameResponse status(@RequestBody int id) {
        return gameService.status(id);
    }

    @PostMapping("/leaveGame")
    public boolean leave(@RequestBody int id) {
        return gameService.leaveGame(id);
    }

    @PostMapping("/notifyWhenMove")
    public DeferredResult<Boolean> notifyWhenMove(@RequestBody int id) {

        DeferredResult<Boolean> deferredResult = new DeferredResult<>((long)1000 * 60 * 2, false);
        CompletableFuture.runAsync(()->{
            while(!gameService.isMyMove(id)) {
                try { Thread.sleep(100); } catch(Exception e) { throw new RuntimeException(e); }
            }
            deferredResult.setResult(true);
        });
        return deferredResult;
    }

    @PostMapping("/games")
    public String games() {
        return gameService.getPlayerGameMapToString();
    }
}
