package net.seanv.stonegameserver.services;

import net.seanv.stonegameserver.entities.User;
import net.seanv.stonegameserver.game.GameSession;
import net.seanv.stonegameserver.repositories.GameResultRepository;
import net.seanv.stonegameserver.repositories.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GameSessionEndServiceTest {
    @Autowired
    private GameSessionEndService service;
    private User user1, user2;
    private GameSession session;

    @BeforeEach
    public void beforeEach(@Autowired UserRepository userRepository) {
        user1 = new User("login1", "password1", "test_user_1");
        user2 = new User("login2", "password2", "test_user_2");

        user1 = userRepository.save(user1);
        user2 = userRepository.save(user2);

        session = new GameSession(user1, user2, service::onSessionEnd);
    }

    @AfterEach
    public void afterEach(@Autowired UserRepository userRepository,
                          @Autowired GameResultRepository gameResultRepository) {
        gameResultRepository.deleteAll();
        userRepository.delete(user1);
        userRepository.delete(user2);
    }

    @Test
    public void testSessionCantEndWhenNotOver() {
        assertThrows(RuntimeException.class, () -> service.onSessionEnd(session));
    }

}