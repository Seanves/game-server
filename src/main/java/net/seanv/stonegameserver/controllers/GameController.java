package net.seanv.stonegameserver.controllers;

import net.seanv.stonegameserver.dto.responses.GameResponse;
import net.seanv.stonegameserver.dto.responses.Opponent;
import net.seanv.stonegameserver.dto.responses.PostGameResult;
import net.seanv.stonegameserver.dto.responses.Response;
import net.seanv.stonegameserver.security.AuthUserContext;
import net.seanv.stonegameserver.services.GameService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
public class GameController {

    private final GameService gameService;
    private final AuthUserContext auth;


    public GameController(GameService gameService, AuthUserContext auth) {
        this.gameService = gameService;
        this.auth = auth;
    }


    @PostMapping("/makeChoose")
    public GameResponse makeChoosingTurn(@RequestBody int amount) {
        return gameService.makeChoosingTurn(auth.getId(), amount);
    }

    @PostMapping("/makeGuess")
    public GameResponse makeGuessingTurn(@RequestBody boolean even) {
        return gameService.makeGuessingTurn(auth.getId(), even);
    }

    @PostMapping("/gameStatus")
    public GameResponse gameStatus() {
        return gameService.getGameStatus(auth.getId());
    }

    @PostMapping("/opponent")
    public Opponent opponent() {
        return gameService.getOpponent(auth.getId());
    }

    @PostMapping("/leaveGame")
    public PostGameResult leave() {
        return gameService.leaveGame(auth.getId());
    }

    @PostMapping("/chatId")
    public int chatId() {
        return gameService.getGameId(auth.getId());
    }


    @PostMapping("/notifyWhenMyTurn")
    public DeferredResult<Response> notifyWhenMyTurn() {
        return gameService.waitOwnTurn(auth.getId());
    }

    @PostMapping("/notifyWhenTurnChanged")
    public DeferredResult<Response> notifyWhenTurnChanged() {
        return gameService.waitTurnChange(auth.getId());
    }


}
