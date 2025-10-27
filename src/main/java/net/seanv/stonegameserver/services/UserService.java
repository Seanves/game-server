package net.seanv.stonegameserver.services;

import net.seanv.stonegameserver.entities.GameResult;
import net.seanv.stonegameserver.dto.responses.GameResultDTO;
import net.seanv.stonegameserver.entities.User;
import net.seanv.stonegameserver.dto.responses.UserInfo;
import net.seanv.stonegameserver.repositories.GameResultRepository;
import net.seanv.stonegameserver.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final GameResultRepository gameResultRepository;

    private final int RESULT_PAGE_SIZE = 10;


    public UserService(UserRepository userRepository, GameResultRepository gameResultRepository) {
        this.userRepository = userRepository;
        this.gameResultRepository = gameResultRepository;
    }


    public UserInfo getUserInfo(User user) {
        return new UserInfo(user, getRank(user.getId()));
    }

    public void changeNickname(User user, String newNickname) {
        user.setNickname(newNickname);
        userRepository.save(user);
    }

    public Page<GameResultDTO> getGameResults(int userId, int page) {
        Pageable pageable = PageRequest.of(page, RESULT_PAGE_SIZE);
        Page<GameResult> results = gameResultRepository.getResultsPage(userId, pageable);

        return results.map(gr -> new GameResultDTO(gr, userId));
    }

    private int getRank(int id) {
        return userRepository.getRank(id).orElseThrow( () -> new IllegalArgumentException("id: " + id) );
    }

    public List<UserInfo> getTop10Ranks() {
        List<User> ranks = userRepository.getTop10Ranks();

        return IntStream.range(0, ranks.size())
                        .mapToObj(i -> new UserInfo(ranks.get(i), i+1))
                        .toList();
    }

}
