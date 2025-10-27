package net.seanv.stonegameserver.services;

import net.seanv.stonegameserver.entities.User;
import net.seanv.stonegameserver.dto.responses.GameResponse;
import net.seanv.stonegameserver.dto.responses.Opponent;
import net.seanv.stonegameserver.game.GameSession;
import net.seanv.stonegameserver.repositories.GameResultRepository;
import net.seanv.stonegameserver.repositories.UserRepository;
import net.seanv.stonegameserver.RandomTestUserCreator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class GameServiceTest {

    @Autowired
    private GameService service;

    private static User user1, user2;


    @BeforeAll
    public static void beforeAll(@Autowired UserRepository userRepository) {
        user1 = RandomTestUserCreator.create();
        user2 = RandomTestUserCreator.create();

        user1 = userRepository.save(user1);
        user2 = userRepository.save(user2);
    }

    @AfterAll
    public static void afterAll(@Autowired UserRepository userRepository,
                                @Autowired GameResultRepository gameResultRepository) {
        gameResultRepository.deleteAll();
        userRepository.deleteAll();
    }

    @BeforeEach
    public void beforeEach() {
        service.createGameSession(user1, user2);
    }


    @Test
    public void testSetup() {
        assertNotNull(service);
        assertTrue(service.isInGame(user1.getId()));
        assertTrue(service.isInGame(user2.getId()));
    }

    @Test
    public void testSessionCreation() {
        int user1gameId = service.getGameId(user1.getId());
        int user2gameId = service.getGameId(user2.getId());

        assertTrue(user1gameId != -1, "not in game");
        assertSame(user1gameId, user2gameId);
    }

    @Test
    public void testTurnOrder() {
        GameResponse user1Status = service.getGameStatus(user1.getId());

        assertTrue(user1Status.yourTurn());
        assertEquals(GameSession.TurnType.CHOOSING, user1Status.turnType());
    }

    @Test
    public void testTurnPassing() {
        service.makeChoosingTurn(user1.getId(), 2);

        GameResponse user2Status = service.getGameStatus(user2.getId());

        assertTrue(user2Status.yourTurn());
        assertEquals(GameSession.TurnType.GUESSING, user2Status.turnType());
    }

    @Test
    public void testTurnTypeSwitchesForSameUser() {
        service.makeChoosingTurn(user1.getId(), 2);
        service.makeGuessingTurn(user2.getId(), true);

        GameResponse user2Status = service.getGameStatus(user2.getId());

        assertTrue(user2Status.yourTurn());
        assertEquals(GameSession.TurnType.CHOOSING, user2Status.turnType());
    }

    @Test
    public void testChoosingTurn() {
        GameResponse response = service.makeChoosingTurn(user1.getId(), 2);

        assertTrue(response.success());
    }

    @Test
    public void testGuessingTurn() {
        service.makeChoosingTurn(user1.getId(), 2);
        service.makeGuessingTurn(user2.getId(), true);
        GameResponse response = service.getGameStatus(user2.getId());

        assertTrue(response.success());
        assertEquals(12, response.yourPoints());
    }

    @Test
    public void testInvalidTurnsUnsuccessful() {
        // wrong turns
        assertFalse(service.makeGuessingTurn(user1.getId(), true).success());
        assertFalse(service.makeGuessingTurn(user2.getId(), true).success());
        // invalid amount
	    assertFalse(service.makeChoosingTurn(user1.getId(), -1).success());
        assertFalse(service.makeChoosingTurn(user1.getId(), 0).success());
        assertFalse(service.makeChoosingTurn(user1.getId(), 11).success());
    }

    @Test
    public void testGettingOpponent() {
        Opponent opponentToUser1 = service.getOpponent(user1.getId());
        assertEquals(user2.getNickname(), opponentToUser1.nickname());
    }

    @Test
    public void testLeaving() {
        service.leaveGame(user1.getId());
        assertFalse(service.isInGame(user1.getId()));
    }

    @Test
    public void testGameEnding() {
        service.makeChoosingTurn(user1.getId(), 10);
        service.makeGuessingTurn(user2.getId(), true);

        GameResponse user1Status = service.getGameStatus(user1.getId());
        GameResponse user2Status = service.getGameStatus(user2.getId());

        assertTrue(user1Status.gameOver());
        assertFalse(user1Status.win());
        assertTrue(user2Status.win());
    }

    @Test
    public void testRatingChange() {
        int user1RatingBefore = user1.getRating();
        int user2RatingBefore = user2.getRating();

        service.makeChoosingTurn(user1.getId(), 10);
        service.makeGuessingTurn(user2.getId(), true);

        assertTrue(user1.getRating() < user1RatingBefore || user1.getRating() == 0);
        assertTrue(user2.getRating() > user2RatingBefore);
    }

    @Test
    public void testPointsOverflow() {
        // move 5 points from 1 to 2 user
        service.makeChoosingTurn(user1.getId(), 5);
        service.makeGuessingTurn(user2.getId(), false);
        // user 1 should give 15 points to user 2 but have less
        service.makeChoosingTurn(user2.getId(), 15);
        service.makeGuessingTurn(user1.getId(), true);

        int user1points = service.getGameStatus(user1.getId()).yourPoints();
        int user2points = service.getGameStatus(user2.getId()).yourPoints();

        assertEquals(20, user2points);
        assertEquals(0, user1points);
    }


}
