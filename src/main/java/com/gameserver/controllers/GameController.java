package com.gameserver.controllers;

import com.gameserver.services.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GameController {

    private final GameService gameService;

    @Autowired
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }


    @GetMapping("makeChoose")
    public String makeMoveChoose(@RequestParam int id, @RequestParam int amount) {
        return gameService.makeChoose(id, amount);
    }

    @GetMapping("makeGuess")
    public String makeMoveGuess(@RequestParam int id, @RequestParam boolean even) {
        return gameService.makeGuess(id, even);
    }
}
