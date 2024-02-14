package com.gameserver.entities;

import com.gameserver.entities.auth.UserDTO;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Entity
@Table(name="Users")
@Data
@NoArgsConstructor
public class User {

    public User(String login, String password, String nickname) {
        this.login = login;
        this.password = password;
        this.nickname = nickname;
        this.rating = 100;
        this.maxRating = this.rating;
    }

    public User(UserDTO dto) {
        this(dto.getLogin(), dto.getPassword(), dto.getNickname());
    }


    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private int id;

    @Column(name = "login", unique = true, nullable = false)
    private String login;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "rating")
    private int rating;

    @Column(name = "max_rating")
    private int maxRating;

    @Column(name = "games_played")
    private int gamesPlayed;

    @Column(name = "wins")
    private int wins;


    public void addRating(int n) {
        rating += n;
        maxRating = Math.max(rating, maxRating);
    }

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
        if(this == o) { return true; }
        if(o == null || getClass() != o.getClass()) { return false; }
        User other = (User) o;
        return id == other.id && Objects.equals(login, other.login)
                              && Objects.equals(nickname, other.nickname);
    }

}
