package com.gameserver.controllers;

import com.gameserver.entities.responses.Status;
import com.gameserver.services.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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


    @PostMapping("/find")
    public int find() {
        int id = nextId();
        gameService.add(id);
        return id;
    }

    @PostMapping("/leaveQueue")
    public boolean leave(@RequestBody int id) {
        return gameService.leaveQueue(id);
    }

    @PostMapping("/queue")
    public String print() {
        return gameService.getQueueToString();
    }

    @PostMapping("/status")
    public Status status(@RequestBody int id) {
        gameService.updateTime(id);
        return new Status(gameService.isInQueue(id), gameService.isInGame(id));
    }

    @PostMapping("/notifyWhenFound")
    public DeferredResult<Boolean> notifyWhenFound(@RequestBody int id) {

        DeferredResult<Boolean> deferredResult = new DeferredResult<>((long)1000 * 60 * 60, false);
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
