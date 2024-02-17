package com.gameserver.websocket;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.gameserver.entities.User;
import com.gameserver.repositories.UserRepository;
import com.gameserver.security.JWTManager;
import com.gameserver.services.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import java.util.NoSuchElementException;

@Component
public class MessageInterceptor implements ChannelInterceptor {

    private final JWTManager jwtManager;
    private final GameService gameService;
    private final UserRepository userRepository;

    @Autowired
    public MessageInterceptor(JWTManager jwtManager, GameService gameService, UserRepository userRepository) {
        this.jwtManager = jwtManager;
        this.gameService = gameService;
        this.userRepository = userRepository;
    }


    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        Object messageType = message.getHeaders().get("simpMessageType");
        if(messageType == SimpMessageType.DISCONNECT) { return message; }
        String authorization = accessor.getFirstNativeHeader("Authorization");


        int userId;
        User user;

        try {
            userId = jwtManager.validateAndGetId(authorization.substring(7));
            user = userRepository.findById(userId).get();
        } catch (JWTVerificationException | NullPointerException
                 | NoSuchElementException e) { return null; } // null means cancel action


        String destination = accessor.getDestination();
        int gameId;

        try {
            int lastSlash = destination.lastIndexOf('/');
            gameId = Integer.parseInt(destination.substring(lastSlash + 1));
        } catch (NumberFormatException e) { return null; }


        if (gameId != gameService.getGameId(user) || gameId == -1) {
            return null;
        }

        return message;
    }

}

