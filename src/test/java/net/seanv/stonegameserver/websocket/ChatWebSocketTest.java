package net.seanv.stonegameserver.websocket;

import net.seanv.stonegameserver.dto.ChatMessage;
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
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import lombok.SneakyThrows;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.DeploymentException;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ChatWebSocketTest {

    @Value("ws://localhost:${local.server.port}/websocket")
    private String url;
    private static WebSocketStompClient stompClient;

    private static String user1Token, user2Token, userNotInGameToken;
    private static int gameId;
    private static String defaultSubscribeDest, defaultSendDest;
    private static User user1, user2;

    private TestClient user1Client, user2Client, testClient;


    private static class TestClient {
        final StompHeaders sendHeaders;
        final LinkedBlockingDeque<ChatMessage> queue;
        final StompSession session;

        TestClient(String token, String url, int chatId) {
            this(token, url, "/chat/" + chatId + "/receive",
                    "/app/chat/" + chatId + "/send", true);
        }

        @SneakyThrows
        TestClient(String token, String url, String subsDest, String sendDest, boolean putToken) {
            var handshakeHeaders = new WebSocketHttpHeaders();
            if (putToken)
                handshakeHeaders.add("Authorization", "Bearer " + token);

            var connectHeaders = new StompHeaders();
            if (putToken)
                connectHeaders.add("Authorization", "Bearer " + token);

            sendHeaders = new StompHeaders();
            sendHeaders.setDestination(sendDest);
            if (putToken)
                sendHeaders.add("Authorization", "Bearer " + token);

            StompHeaders subscribeHeaders = new StompHeaders();
            subscribeHeaders.setDestination(subsDest);
            if (putToken)
                subscribeHeaders.add("Authorization", "Bearer " + token);

            queue = new LinkedBlockingDeque<>();

            session = stompClient
                    .connect(url, handshakeHeaders, connectHeaders, new StompSessionHandlerAdapter(){})
                    .get(5, TimeUnit.SECONDS);

            session.subscribe(subscribeHeaders, new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) { return ChatMessage.class; }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    ChatMessage message = (ChatMessage) payload;
                    queue.offer(message);
                }
            });
        }

        void send(String text) {
            session.send(sendHeaders, text);
        }

        @SneakyThrows
        ChatMessage pollMessage() {
            return queue.poll(2, TimeUnit.SECONDS);
        }
    }

    @BeforeAll
    public static void beforeAll(@Autowired UserRepository userRepository,
                                 @Autowired GameService gameService,
                                 @Autowired AuthenticationService authService) {

        AuthResponse register1 = authService.register(new UserAuthDTO("unique_login1", "password1", "test_user_1"));
        AuthResponse register2 = authService.register(new UserAuthDTO("unique_login2", "password2", "test_user_2"));
        AuthResponse register3 = authService.register(new UserAuthDTO("unique_login3", "password3", "test_user_3"));

        user1 = userRepository.findByLogin("unique_login1")
                                    .orElseThrow(() -> new RuntimeException("user 1 not found"));
        user2 = userRepository.findByLogin("unique_login2")
                                    .orElseThrow(() -> new RuntimeException("user 2 not found"));

        gameService.createGameSession(user1, user2);
        gameId = gameService.getGameId(user1.getId());

        defaultSubscribeDest = "/chat/" + gameId + "/receive";
        defaultSendDest = "/app/chat/" + gameId + "/send";

        user1Token = Objects.requireNonNull(register1.token());
        user2Token = Objects.requireNonNull(register2.token());
        userNotInGameToken = Objects.requireNonNull(register3.token());

        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    @AfterAll
    public static void afterAll(@Autowired UserRepository userRepository) {
        userRepository.deleteAll();
    }

    @BeforeEach
    void beforeEach() {
        user1Client = new TestClient(user1Token, url, gameId);
        user2Client = new TestClient(user2Token, url, gameId);
        testClient = null;
    }

    @AfterEach
    void afterEach() {
        user1Client.session.disconnect();
        user2Client.session.disconnect();
        if (testClient != null) {
            testClient.session.disconnect();
        }
    }

    @Test
    void testSendAndReceiveMessage() {
        String text = "Hello from user1!";
        user1Client.send(text);

        ChatMessage receivedByUser1 = user1Client.pollMessage();
        ChatMessage receivedByUser2 = user2Client.pollMessage();
        ChatMessage expected = new ChatMessage(user1.getNickname(), text);

        assertNotNull(receivedByUser1);
        assertNotNull(receivedByUser2);
        assertEquals(expected, receivedByUser1);
        assertEquals(expected, receivedByUser2);
    }

    @Test
    void testInvalidTokenConnectionFail() {
        Exception ex = assertThrows(ExecutionException.class,
                () -> new TestClient("invalid", url, gameId));
        assertInstanceOf(DeploymentException.class, ex.getCause());
    }

    @Test
    void testSendToInvalidChatFail() {
        testClient = new TestClient(user1Token, url, gameId + 1);
        testClient.send("message");
        assertNull(testClient.pollMessage());
    }

    @Test
    void testUserNotInGameCantReceiveMessage() {
        testClient = new TestClient(userNotInGameToken, url, gameId);
        user1Client.send("message");
        // message shouldn't be sent to user not in game
        assertNull(testClient.pollMessage());
    }

    @Test
    void testUserNotInGameCantSendMessage() {
        testClient = new TestClient(userNotInGameToken, url, gameId);
        testClient.send("message");
        // user 1 shouldn't receive message
        assertNull(user1Client.pollMessage());
    }

    @Test
    void testSendAndReceiveMessageAfterInvalidClientConnect() {
        assertThrows(ExecutionException.class, () -> new TestClient("", url, gameId));

        String text = "Hello from user1!";
        user1Client.send(text);

        ChatMessage receivedByUser2 = user2Client.pollMessage();
        ChatMessage expected = new ChatMessage(user1.getNickname(), text);

        assertNotNull(receivedByUser2);
        assertEquals(expected, receivedByUser2);
    }

    @Test
    void testConnectWithoutTokenFail() {
        Exception ex = assertThrows(ExecutionException.class, () ->
            new TestClient("", url, defaultSubscribeDest,
                                defaultSendDest, false)
        );
        assertInstanceOf(DeploymentException.class, ex.getCause());
    }

    @Test
    void testSubscribeOnSendPathFail() {
        testClient = new TestClient(user1Token, url, defaultSendDest,
                                        defaultSendDest, true);

        user1Client.send("message");
        assertNull(testClient.pollMessage());
    }

    @Test
    void testCantSendFakeMessageOnReceivePath() throws JsonProcessingException {
        testClient = new TestClient(user1Token, url, defaultSubscribeDest,
                                                defaultSubscribeDest, true);

        ChatMessage fakeMessage = new ChatMessage("fake_name", "hello");
        testClient.send(new ObjectMapper().writeValueAsString(fakeMessage));

        assertNull(user1Client.pollMessage());
    }

}
