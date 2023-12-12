package com.gameserver.entities.responses;

import com.gameserver.entities.User;
import lombok.Getter;

@Getter
public class Stats {

    public Stats(User user, int rank) {
        this.rating = user.getRating();
        this.winrate = String.format("%.2f", user.getWinrate()) + "%";
        this.rank = "№" + rank;
        this.gamesPlayed = user.getGamesPlayed();
        this.wins = user.getWins();
    }

    private int rating;
    private String winrate;
    private String rank;
    private int gamesPlayed;
    private int wins;
}
