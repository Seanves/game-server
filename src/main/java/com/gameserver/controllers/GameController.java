package com.gameserver.controllers;

import com.gameserver.entities.responses.GameResponse;
import com.gameserver.entities.responses.Opponent;
import com.gameserver.entities.responses.PostGameResult;
import com.gameserver.entities.responses.Response;
import com.gameserver.security.AuthenticatedUserContext;
import com.gameserver.services.GameService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
public class GameController {

    private final GameService gameService;
    private final AuthenticatedUserContext auth;


    public GameController(GameService gameService, AuthenticatedUserContext auth) {
        this.gameService = gameService;
        this.auth = auth;
    }


    @PostMapping("/makeChoose")
    public GameResponse makeMoveChoose(@RequestBody int amount) {
        return gameService.makeMoveChoose(auth.getId(), amount);
    }

    @PostMapping("/makeGuess")
    public GameResponse makeMoveGuess(@RequestBody boolean even) {
        return gameService.makeMoveGuess(auth.getId(), even);
    }

    @PostMapping("/gameStatus")
    public GameResponse status() {
        return gameService.getStatus(auth.getId());
    }

    @PostMapping("/opponent")
    public Opponent opponent() {
        return gameService.getOpponent(auth.getId());
    }

    @PostMapping("/leaveGame")
    public PostGameResult leave() { return gameService.leaveGame(auth.getId()); }

    @PostMapping("/chatId")
    public int chatId() { return gameService.getGameId(auth.getId()); }


    @PostMapping("/notifyWhenMyMove")
    public DeferredResult<Response> notifyWhenMyMove() {
        return gameService.waitForMyMove(auth.getId());
    }

    @PostMapping("/notifyWhenMoveChanged")
    public DeferredResult<Response> notifyWhenMoveChanged() {
        return gameService.waitForMoveChange(auth.getId());
    }


}
