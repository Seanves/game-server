package net.seanv.stonegameserver.game;

import net.seanv.stonegameserver.entities.User;
import lombok.Getter;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


public class GameAcceptance {

    @Getter
    private final User user1,
                       user2;

    private boolean user1accepted,
                    user2accepted,
                    user1declined,
                    user2declined;

    private final ScheduledFuture<?> scheduledFuture;
    private static final long TIMEOUT = 30; // seconds

    public GameAcceptance(User user1, User user2, Consumer<GameAcceptance> deleteAcceptanceCallback) {
        this.user1 = user1;
        this.user2 = user2;

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        scheduledFuture = executorService.schedule(
                () -> deleteAcceptanceCallback.accept(this), TIMEOUT, TimeUnit.SECONDS);
    }


    public void accept(int id) {
        if      (id == user1.getId()) { user1accepted = true; }
        else if (id == user2.getId()) { user2accepted = true; }
        else                          { throw new IllegalArgumentException("id " + id); }
    }

    public void decline(int id) {
        if      (id == user1.getId()) { user1declined = true; }
        else if (id == user2.getId()) { user2declined = true; }
        else                          { throw new IllegalArgumentException("id " + id); }
    }

    public boolean isEverybodyAccepted() {
        return user1accepted && user2accepted;
    }

    public boolean isAnybodyDeclined() {
        return user1declined || user2declined;
    }
}
