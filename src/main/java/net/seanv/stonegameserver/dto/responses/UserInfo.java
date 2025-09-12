package net.seanv.stonegameserver.dto.responses;

import net.seanv.stonegameserver.entities.User;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserInfo {

    private String nickname;
    private int rating;
    private double winrate;
    private int rank;
    private int gamesPlayed;
    private int wins;

    public UserInfo(User user, int rank) {
        this.nickname = user.getNickname();
        this.rating = user.getRating();
        this.winrate = user.getWinrate();
        this.rank = rank;
        this.gamesPlayed = user.getGamesPlayed();
        this.wins = user.getWins();
    }
}
