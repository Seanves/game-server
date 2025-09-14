package net.seanv.stonegameserver.services;

import net.seanv.stonegameserver.entities.GameResult;
import net.seanv.stonegameserver.entities.User;
import net.seanv.stonegameserver.game.GameSession;
import net.seanv.stonegameserver.repositories.GameResultRepository;
import net.seanv.stonegameserver.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GameSessionEndService {
    private final UserRepository userRepository;
    private final GameResultRepository gameResultRepository;


    public GameSessionEndService(UserRepository userRepository, GameResultRepository gameResultRepository) {
        this.userRepository = userRepository;
        this.gameResultRepository = gameResultRepository;
    }

    private int countRatingChange(User winner, User loser) {
        // multiplier
        double m = (double) Math.max(loser.getRating(), 20) / Math.max(winner.getRating(), 20);
        // multiplier closer to 1 by 80%
        m = m + (1 - m) * 0.8;
        // rating change
        int change = (int)(m * 20);
        // min 10, max 40
        change = Math.min(Math.max(change, 10), 40);

        return change;
    }

    @Transactional
    public int onSessionEnd(GameSession game) {
        if (game.isOver()) {
            throw new RuntimeException("session already ended");
        }

        User winner = game.getWinner();
        User loser = game.getLoser();

        int loserRatingBefore = loser.getRating();
        int ratingChange = countRatingChange(winner, loser);

        winner.addRating(ratingChange);
        loser.subtractRating(ratingChange);

        int loserRatingChange = loser.getRating() - loserRatingBefore;

        winner.incrementWins();
        winner.incrementGamesPlayed();
        loser.incrementGamesPlayed();

        GameResult result = new GameResult(winner, loser, ratingChange, loserRatingChange);
        gameResultRepository.save(result);

        userRepository.save(winner);
        userRepository.save(loser);

        return ratingChange;
    }
}
