package com.gameserver.entities.responses;

import com.gameserver.entities.User;
import lombok.Data;

@Data
public class Opponent {

    public Opponent(User user) {
        this.nickname = user.getNickname();
        this.rating = user.getRating();
    }

    private String nickname;
    private int rating;
}
