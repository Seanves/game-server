package com.gameserver.controllers;

import com.gameserver.services.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@RestController
public class GameController {

    private final GameService gameService;

    @Autowired
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }


    @GetMapping("/add")
    public boolean add(@RequestParam String ip) {
        return gameService.add(ip);
    }

    @GetMapping("/queue")
    public String print() {
        return gameService.getQueueToString();
    }

    @GetMapping("/games")
    public String games() {
        return gameService.getIpGameMapToString();
    }

    @GetMapping("/status")
    public String status(@RequestParam String ip) {
        if(gameService.isInQueue(ip)) {
            gameService.updateTime(ip);
            return "in queue";
        }
        return "not in queue";
    }

    @GetMapping("/notify")
    public DeferredResult<Boolean> notifyWhenGameFound(@RequestParam String ip) {

        DeferredResult<Boolean> deferredResult = new DeferredResult<>((long)1000 * 30, false);
        CompletableFuture.runAsync(()->{
            while(gameService.isInQueue(ip)) {
                try { Thread.sleep(100); } catch(Exception e) { throw new RuntimeException(e); }
            }
            deferredResult.setResult(gameService.isInGame(ip));
        });
        return deferredResult;
    }
}
