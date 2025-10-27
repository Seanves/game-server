package net.seanv.stonegameserver.game;

import java.util.function.Consumer;
import java.util.function.Function;

public class GameCallback {
    private final Function<GameSession, Integer> onSessionEnd;
    private final Consumer<GameSession> onTurnChange;

    public GameCallback(Function<GameSession, Integer> onSessionEnd,
                        Consumer<GameSession> onTurnChange) {
        this.onSessionEnd = onSessionEnd;
        this.onTurnChange = onTurnChange;
    }

    public int onSessionEnd(GameSession game) {
        return onSessionEnd.apply(game);
    }

    public void onTurnChange(GameSession game) {
        onTurnChange.accept(game);
    }
}
