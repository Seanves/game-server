package com.gameserver.controllers;

import com.gameserver.services.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import java.util.concurrent.CompletableFuture;

@RestController
public class QueueController {

    private final GameService gameService;

    @Autowired
    public QueueController(GameService gameService) {
        this.gameService = gameService;
    }


    @GetMapping("/add")
    public int add() {
        int id = nextId();
        return gameService.add(id) ?  id : -1;
    }

    @GetMapping("/queue")
    public String print() {
        return gameService.getQueueToString();
    }

    @GetMapping("/games")
    public String games() {
        return gameService.getPlayerGameMapToString();
    }

    @GetMapping("/status")
    public String status(@RequestParam int id) {
        if(gameService.isInQueue(id)) {
            gameService.updateTime(id);
            return "in queue";
        }
        return "not in queue";
    }

    @GetMapping("/notify")
    public DeferredResult<Boolean> notifyWhenFound(@RequestParam int id) {

        DeferredResult<Boolean> deferredResult = new DeferredResult<>((long)1000 * 30, false);
        CompletableFuture.runAsync(()->{
            while(gameService.isInQueue(id)) {
                try { Thread.sleep(100); } catch(Exception e) { throw new RuntimeException(e); }
            }
            deferredResult.setResult(gameService.isInGame(id));
        });
        return deferredResult;
    }


    private int idCounter = 0;

    private synchronized int nextId() {
        return ++idCounter;
    }
}
