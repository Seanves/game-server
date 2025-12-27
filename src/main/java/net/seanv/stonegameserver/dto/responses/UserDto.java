package net.seanv.stonegameserver.dto.responses;

import lombok.Data;

@Data
public class UserDto {

    private String nickname;
    private int rating;
    private double winrate;
    private int rank;
    private int gamesPlayed;
    private int wins;

}
