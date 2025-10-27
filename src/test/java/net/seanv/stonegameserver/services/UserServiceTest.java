package net.seanv.stonegameserver.services;

import net.seanv.stonegameserver.entities.GameResult;
import net.seanv.stonegameserver.entities.User;
import net.seanv.stonegameserver.dto.responses.GameResultDTO;
import net.seanv.stonegameserver.dto.responses.UserInfo;
import net.seanv.stonegameserver.repositories.GameResultRepository;
import net.seanv.stonegameserver.repositories.UserRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserService service;

    private static User user1, user2, user3, user4;

    @BeforeAll
    public static void beforeAll(@Autowired UserRepository userRepository,
                                 @Autowired GameResultRepository gameResultRepository) {

        user1 = new User("login1", "password", "name1");
        user1.setRating(80);
        user1 = userRepository.save(user1);

        user2 = new User("login2", "password", "name2");
        user2.setRating(10);
        user2 = userRepository.save(user2);

        user3 = new User("login3", "password", "name3");
        user3.setRating(110);
        user3 = userRepository.save(user3);

        user4 = new User("login4", "password", "name4");
        user4.setRating(10);
        user4 = userRepository.save(user4);


        GameResult[] results = {
            new GameResult(user1, user2, 0, 0),
            new GameResult(user1, user2, 0, 0),
            new GameResult(user2, user1, 0, 0)
        };

        for (var result : results) {
            gameResultRepository.save(result);
        }

    }

    @AfterAll
    public static void afterAll(@Autowired UserRepository userRepository,
                                @Autowired GameResultRepository gameResultRepository) {
        gameResultRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void testTop10order() {
        List<UserInfo> ranks = service.getTop10Ranks();
        List<UserInfo> expected = List.of(new UserInfo(user3, 1), new UserInfo(user1, 2),
                                          new UserInfo(user2, 3), new UserInfo(user4, 4));
        assertIterableEquals(expected, ranks);
    }

    @Test
    public void testGetRank() {
        Assertions.assertEquals(1, service.getUserInfo(user3).getRank());
        Assertions.assertEquals(2, service.getUserInfo(user1).getRank());
        Assertions.assertEquals(3, service.getUserInfo(user2).getRank());
        Assertions.assertEquals(4, service.getUserInfo(user4).getRank());
    }

    @Test
    public void testUserInfoCorrectInformation() {
        UserInfo info = service.getUserInfo(user1);

        assertEquals(user1.getNickname(),    info.getNickname());
        assertEquals(user1.getRating(),      info.getRating());
        assertEquals(user1.getWinrate(),     info.getWinrate());
        assertEquals(user1.getWins(),        info.getWins());
        assertEquals(user1.getGamesPlayed(), info.getGamesPlayed());
    }

    @Test
    public void testExceptionForInvalidUserInfo() {
        assertThrows(IllegalArgumentException.class, () -> service.getUserInfo(new User()));
    }

    @Test
    public void testChangingNickname(@Autowired UserRepository repository) {
        User user = new User("login_", "password", "name_to_change");
        user = repository.save(user);

        service.changeNickname(user, "new_nickname");
        Optional<User> optional = repository.findById(user.getId());

        assertTrue(optional.isPresent());
        assertEquals(user.getNickname(), optional.get().getNickname());
    }

    @Test
    public void testGameResultsSize() {
        Page<GameResultDTO> user1Results = service.getGameResults(user1.getId(), 1);
        Page<GameResultDTO> user2Results = service.getGameResults(user2.getId(), 1);
        Page<GameResultDTO> user3Results = service.getGameResults(user3.getId(), 1);

        assertEquals(3, user1Results.getTotalElements());
        assertEquals(3, user2Results.getTotalElements());
        assertEquals(0, user3Results.getTotalElements());
    }

    @Test
    public void testGameResultContent() {
        List<GameResultDTO> results = service.getGameResults(user1.getId(), 0).get().toList();

        assertSame(3, results.size());

        assertFalse(results.get(0).isWin());
        assertTrue(results.get(1).isWin());
        assertTrue(results.get(2).isWin());

        results.forEach(r ->
                assertEquals(user2.getNickname(), r.getOpponentNickname()));
    }

}
