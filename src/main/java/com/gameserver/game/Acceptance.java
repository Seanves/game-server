package com.gameserver.game;

import com.gameserver.entities.User;
import com.gameserver.entities.responses.Response;
import lombok.Getter;

import java.util.concurrent.*;


@Getter
public class Acceptance {

    private final User user1,
                       user2;

    private boolean user1accepted,
                    user2accepted,
                    user1declined,
                    user2declined;

    private ScheduledFuture<?> scheduledFuture;
    private static final long TIMEOUT = 30; // seconds

    public Acceptance(User user1, User user2, DeleteAcceptanceCallback callback) {
        this.user1 = user1;
        this.user2 = user2;

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        scheduledFuture = executorService.schedule( () -> callback.deleteAcceptance(this), TIMEOUT, TimeUnit.SECONDS);
    }


    public void accept(User user) {
        if(user.equals(user1)) { user1accepted = true; }
        else if(user.equals(user2)) { user2accepted = true; }
    }

    public void decline(User user) {
        if(user.equals(user1)) { user1declined = true; }
        else if(user.equals(user2)) { user2declined = true; }
    }

    public boolean isEverybodyAccepted() {
        return user1accepted && user2accepted;
    }

    public boolean isAnybodyDeclined() {
        return user1declined || user2declined;
    }
}
