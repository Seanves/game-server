package net.seanv.stonegameserver;

import net.seanv.stonegameserver.entities.User;
import java.util.UUID;

public class RandomTestUserCreator {
    public static User create() {
        return new User(randomString(20), randomString(18) + "&1", randomString(10));
    }

    private static String randomString(int length) {
        return UUID.randomUUID().toString().substring(0, length);
    }
}
