package net.seanv.stonegameserver.services;

import net.seanv.stonegameserver.entities.User;
import net.seanv.stonegameserver.repositories.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SpringBootTest
public class MatchmakingLongPollingTest {
    @Autowired
    private MatchmakingService service;
    private static User user1, user2;

    @BeforeAll
    public static void beforeAll(@Autowired UserRepository repository) {
        user1 = repository.save(new User(UUID.randomUUID().toString().substring(0, 19), " ", " "));
        user2 = repository.save(new User(UUID.randomUUID().toString().substring(0, 19), " ", " "));
    }

    @AfterEach
    public void afterEach() {
        service.leaveQueue(user1.getId());
        service.leaveQueue(user2.getId());
        service.declineGame(user1.getId());
        service.declineGame(user2.getId());
    }


    @Test
    public void testWaitUntilGameFound() {
        var response = service.addInQueue(user1);
        assumeTrue(response.success());

        var result = service.waitUntilGameFound(user1.getId());
        assertNull(result.getResult());

        service.addInQueue(user2);
        assumeTrue(service.isInAcceptance(user1.getId()));

        assertNotNull(result.getResult());
    }

    @Test
    public void testWaitWhileInAcceptance() {
        service.addInQueue(user1);
        service.addInQueue(user2);
        assumeTrue(service.isInAcceptance(user1.getId()));

        var result = service.waitWhileInAcceptance(user1.getId());
        assertNull(result.getResult());

        assumeTrue(service.declineGame(user1.getId()));

        assertNotNull(result.getResult());
    }

    @Test
    public void testWaitWhileInAcceptanceImmediateResponse() {
        var result = service.waitWhileInAcceptance(user1.getId());
        assertNotNull(result);
    }

}
