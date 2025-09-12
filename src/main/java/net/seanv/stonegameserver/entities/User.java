package net.seanv.stonegameserver.entities;

import net.seanv.stonegameserver.dto.auth.UserAuthDTO;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Entity
@Table(name="Users", indexes = {
    @Index(name = "idx_ranks", columnList = "rating DESC, maxRating DESC, nickname")
})
@Data
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private int id;

    @Column(unique = true, nullable = false, length = 20)
    private String login;

    @Column(nullable = false, length = 60)
    private String password;

    @Column(nullable = false, length = 20)
    private String nickname;

    private int rating;
    private int maxRating;
    private int gamesPlayed;
    private int wins;


    public User(String login, String password, String nickname) {
        this.login = login;
        this.password = password;
        this.nickname = nickname;
    }

    public User(UserAuthDTO dto) {
        this(dto.getLogin(), dto.getPassword(), dto.getNickname());
    }


    public void addRating(int n) {
        rating += n;
        maxRating = Math.max(rating, maxRating);
    }

    public void subtractRating(int n) { rating = Math.max(rating-n, 0); }

    public void incrementGamesPlayed() { gamesPlayed++; }

    public void incrementWins() { wins++; }

    public double getWinrate() {
        if (wins == 0) { return 0; }
        return (double) wins / gamesPlayed * 100;
    }


    @Override
    public int hashCode() {
        return Objects.hash(id, login, nickname);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        User other = (User) o;
        return id == other.id
                && Objects.equals(login, other.login)
                && Objects.equals(nickname, other.nickname);
    }

}
