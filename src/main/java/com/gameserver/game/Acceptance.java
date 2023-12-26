package com.gameserver.game;

import com.gameserver.entities.User;
import lombok.Getter;


@Getter
public class Acceptance {

    private final User user1,
                       user2;

    private boolean user1accepted,
                    user2accepted,
                    user1declined,
                    user2declined;

    private final Thread timeoutDeleteThis;
    private static final long TIMEOUT = 1000 * 30;

    public Acceptance(User user1, User user2, DeleteAcceptanceCallback callback) {
        this.user1 = user1;
        this.user2 = user2;

        this.timeoutDeleteThis = new Thread( () -> {
            try {
                Thread.sleep(TIMEOUT);
                callback.deleteAcceptance(this);
            }
            catch(InterruptedException e) { Thread.currentThread().interrupt(); }
        } );
        this.timeoutDeleteThis.start();
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
