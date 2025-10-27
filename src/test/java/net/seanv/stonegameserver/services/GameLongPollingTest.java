package net.seanv.stonegameserver.services;

import net.seanv.stonegameserver.RandomTestUserCreator;
import net.seanv.stonegameserver.entities.User;
import net.seanv.stonegameserver.repositories.GameResultRepository;
import net.seanv.stonegameserver.repositories.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

@SpringBootTest
public class GameLongPollingTest {
    @Autowired
    private GameService service;
    private static User user1, user2;

    @BeforeAll
    public static void beforeAll(@Autowired UserRepository repository) {
        user1 = repository.save(RandomTestUserCreator.create());
        user2 = repository.save(RandomTestUserCreator.create());
    }

    @AfterAll
    public static void afterAll(@Autowired UserRepository repository) {
        repository.deleteAll(List.of(user1, user2));
    }

    @BeforeEach
    public void beforeEach() {
        service.createGameSession(user1, user2);
    }

    @AfterEach
    public void afterEach(@Autowired GameResultRepository gameResultRepository) {
        service.leaveGame(user1.getId());
        service.leaveGame(user2.getId());
        gameResultRepository.deleteAll();
    }


    @Test
    public void testTurnChangeChoosing() {
        var result = service.waitTurnChange(user1.getId());
        assertNull(result.getResult());

        var response = service.makeChoosingTurn(user1.getId(), 1);
        assumeTrue(response.success());

        assertNotNull(result.getResult());
    }

    @Test
    public void testTurnChangeGuessing() {
        service.makeChoosingTurn(user1.getId(), 1);
        assumeTrue(service.isMyTurn(user2.getId()));

        var result = service.waitTurnChange(user2.getId());
        assertNull(result.getResult());

        var response = service.makeGuessingTurn(user2.getId(), true);
        assumeTrue(response.success());

        assertNotNull(result.getResult());
    }

    @Test
    public void testWaitOwnTurn() {
        var result = service.waitOwnTurn(user2.getId());
        assertNull(result.getResult());

        var response = service.makeChoosingTurn(user1.getId(), 1);
        assumeTrue(response.success());

        assertNotNull(result.getResult());
    }

    @Test
    public void testWaitOwnTurnImmediateResponse() {
        assumeTrue(service.isMyTurn(user1.getId()));
        assertNotNull(service.waitOwnTurn(user1.getId()));
    }

}
