package net.seanv.stonegameserver.dto.responses;

import net.seanv.stonegameserver.entities.User;


public record Opponent(String nickname, int rating) {
    public Opponent(User user) {
        this(user.getNickname(), user.getRating());
    }
}
