package net.seanv.stonegameserver.services;

import net.seanv.stonegameserver.entities.User;
import net.seanv.stonegameserver.dto.responses.Response;
import net.seanv.stonegameserver.repositories.UserRepository;
import net.seanv.stonegameserver.RandomTestUserCreator;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MatchmakingServiceTest {

    @Autowired
    private MatchmakingService service;

    private static User user1, user2, user3;


    @BeforeAll
    public static void beforeAll(@Autowired UserRepository userRepository) {
        user1 = RandomTestUserCreator.create();
        user2 = RandomTestUserCreator.create();
        user3 = RandomTestUserCreator.create();

        user1 = userRepository.save(user1);
        user2 = userRepository.save(user2);
        user3 = userRepository.save(user3);
    }

    @AfterAll
    public static void afterAll(@Autowired UserRepository userRepository) {
        userRepository.deleteAll();
    }

    @BeforeEach
    public void beforeEach() {
        service.addInQueue(user1);
        service.addInQueue(user2);
    }

    @AfterEach
    public void afterEach() {
        service.leaveQueue(user3.getId());

        service.declineGame(user1.getId());
        service.declineGame(user2.getId());
    }

    @Test
    public void testSetup() {
        assertTrue(service.isInAcceptance(user1.getId()));
        assertTrue(service.isInAcceptance(user2.getId()));
        assertFalse(service.isInQueue(user1.getId()));
        assertFalse(service.isInQueue(user2.getId()));
    }

    @Test
    public void testAddingInQueue() {
        service.addInQueue(user3);
        assertTrue(service.isInQueue(user3.getId()));
    }

    @Test
    @DirtiesContext
    public void testAcceptGame(@Autowired GameService gameService) {
        service.acceptGame(user1.getId());
        service.acceptGame(user2.getId());

        assertTrue(gameService.isInGame(user1.getId()));
        assertTrue(gameService.isInGame(user2.getId()));

        assertFalse(service.isInAcceptance(user1.getId()));
        assertFalse(service.isInAcceptance(user2.getId()));
    }

    @Test
    public void testAcceptGameReturn() {
        assertTrue(service.acceptGame(user1.getId()));
        assertFalse(service.acceptGame(user3.getId()));
    }

    @Test
    public void testDeclineGameAndAcceptanceDelete() {
        service.declineGame(user1.getId());

        assertFalse(service.isInAcceptance(user1.getId()));
        assertFalse(service.isInAcceptance(user2.getId()));
    }

    @Test
    public void testDeclineGameReturn() {
        assertFalse(service.declineGame(user3.getId()));
        assertTrue(service.declineGame(user1.getId()));
    }

    @Test
    public void testLeaveGame() {
        service.addInQueue(user3);
        service.leaveQueue(user3.getId());
        assertFalse(service.isInQueue(user3.getId()));
    }

    @Test
    public void testAddInQueueWhenInQueue() {
        service.addInQueue(user3);
        Response response = service.addInQueue(user3);
        assertSame(response, Response.ALREADY_IN_QUEUE);
    }

    @Test
    @SneakyThrows
    public void testTimeoutFromQueue(@Value("${QUEUE_TIMEOUT}") int timeout) {
        service.addInQueue(user3);

        Thread.sleep((int) (timeout * 1.5));

        assertFalse(service.isInQueue(user3.getId()));
    }

    @Test
    public void testAddInQueueWhenInAcceptance() {
        Response response = service.addInQueue(user1);
        assertSame(response, Response.ALREADY_IN_ACCEPTANCE);
    }


}
