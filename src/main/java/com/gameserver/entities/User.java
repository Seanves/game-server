package com.gameserver.entities;

import com.gameserver.entities.auth.UserDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity @Data
@Table(name="Users")
@NoArgsConstructor
public class User {

    public User(String login, String password, String nickname) {
        this.login = login;
        this.password = password;
        this.nickname = nickname;
        this.rating = 100;
    }

    public User(UserDto dto) {
        this(dto.getLogin(), dto.getPassword(), dto.getNickname());
    }

    @Id @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "login")
    private String login;

    @Column(name = "password")
    private String password;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "rating")
    private int rating;

    @Column(name = "games_played")
    private int gamesPlayed;

    @Column(name = "wins")
    private int wins;


    public void addRating(int n) { rating += n; }

    public void subtractRating(int n) { rating = Math.max(rating-n, 1); }

    public void incrementGamesPlayed() { gamesPlayed++; }

    public void incrementWins() { wins++; }

    public double getWinrate() {
        if(wins==0) { return 0.0; }
        return (double) wins / gamesPlayed * 100;
    }


    @Override
    public int hashCode() {
        return Objects.hash(id, login, nickname);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        User other = (User) o;
        return id == other.id && Objects.equals(login, other.login) && Objects.equals(nickname, other.nickname);
    }

}
