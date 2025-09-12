package net.seanv.stonegameserver.websocket;

import net.seanv.stonegameserver.entities.User;
import net.seanv.stonegameserver.dto.auth.AuthResponse;
import net.seanv.stonegameserver.dto.auth.UserAuthDTO;
import net.seanv.stonegameserver.repositories.UserRepository;
import net.seanv.stonegameserver.services.AuthenticationService;
import net.seanv.stonegameserver.services.GameService;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import lombok.SneakyThrows;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ChatWebSocketTest {

    @Value("ws://localhost:${local.server.port}/websocket")
    private String url;
    private static WebSocketStompClient stompClient;

    private static String user1Token, user2Token, userNotInGameToken;
    private static int gameId;

    private TestClient user1Client, user2Client;


    private static class TestClient {
        final StompHeaders roomHeaders;
        final BlockingQueue<String> queue;
        final StompSession session;

        @SneakyThrows
        TestClient(String token, String url, int roomId) {
            var handshakeHeaders = new WebSocketHttpHeaders();
            handshakeHeaders.add("Authorization", "Bearer " + token);

            var connectHeaders = new StompHeaders();
            connectHeaders.add("Authorization", "Bearer " + token);

            roomHeaders = new StompHeaders();
            roomHeaders.setDestination("/room/" + roomId);
            roomHeaders.add("Authorization", "Bearer " + token);

            queue = new ArrayBlockingQueue<>(1);

            session = stompClient
                    .connect(url, handshakeHeaders, connectHeaders, new StompSessionHandlerAdapter(){})
                    .get(5, TimeUnit.SECONDS);

            session.subscribe(roomHeaders, new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) { return String.class; }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    queue.offer((String) payload);
                }
            });
        }

        void send(String message) {
            session.send(roomHeaders, message);
        }
    }

    @BeforeAll
    public static void beforeAll(@Autowired UserRepository userRepository,
                                 @Autowired GameService gameService,
                                 @Autowired AuthenticationService authService) {

        AuthResponse register1 = authService.register(new UserAuthDTO("unique_login1", "password1", "test_user_1"));
        AuthResponse register2 = authService.register(new UserAuthDTO("unique_login2", "password2", "test_user_2"));
        AuthResponse register3 = authService.register(new UserAuthDTO("unique_login3", "password3", "test_user_3"));

        User user1 = userRepository.findByLogin("unique_login1")
                                    .orElseThrow(() -> new RuntimeException("user 1 not found"));
        User user2 = userRepository.findByLogin("unique_login2")
                                    .orElseThrow(() -> new RuntimeException("user 2 not found"));

        gameService.createGameSession(user1, user2);
        gameId = gameService.getGameId(user1.getId());

        user1Token = Objects.requireNonNull(register1.token());
        user2Token = Objects.requireNonNull(register2.token());
        userNotInGameToken = Objects.requireNonNull(register3.token());

        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new StringMessageConverter());
    }

    @AfterAll
    public static void afterAll(@Autowired UserRepository userRepository) {
        userRepository.deleteAll();
    }

    @BeforeEach
    void setup() {
        user1Client = new TestClient(user1Token, url, gameId);
        user2Client = new TestClient(user2Token, url, gameId);
    }

    @AfterEach
    void afterEach() {
        user1Client.session.disconnect();
        user2Client.session.disconnect();
    }

    @Test
    void testSendAndReceiveMessage() throws Exception {
        String message = "Hello from user1!";
        user1Client.send(message);

        String receivedByUser1 = user1Client.queue.poll(1, TimeUnit.SECONDS);
        String receivedByUser2 = user2Client.queue.poll(1, TimeUnit.SECONDS);

        assertNotNull(receivedByUser1);
        assertNotNull(receivedByUser2);
        assertEquals(message, receivedByUser1);
        assertEquals(message, receivedByUser2);
    }

    @Test
    void testInvalidTokenConnectionFail() {
        assertThrows(Exception.class,
                () -> new TestClient("invalid", url, gameId));
    }

    @Test
    void testConnectionToInvalidRoomFail() throws InterruptedException {
        TestClient testClient = new TestClient(user1Token, url, gameId + 1);
        testClient.send("message");
        assertNull(testClient.queue.poll(1, TimeUnit.SECONDS));
    }

    @Test
    void testUserNotInGameCantReceiveMessage() throws InterruptedException {
        TestClient testClient = new TestClient(userNotInGameToken, url, gameId);
        user1Client.send("message");
        // message shouldn't be sent to user not in game
        assertNull(testClient.queue.poll(1, TimeUnit.SECONDS));
    }

    @Test
    void testUserNotInGameCantSendMessage() throws InterruptedException {
        TestClient testClient = new TestClient(userNotInGameToken, url, gameId);
        testClient.send("message");
        // user 1 shouldn't receive message
        assertNull(user1Client.queue.poll(1, TimeUnit.SECONDS));
    }


}
