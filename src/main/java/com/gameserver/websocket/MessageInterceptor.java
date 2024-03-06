package com.gameserver.websocket;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.gameserver.security.JWTManager;
import com.gameserver.services.GameService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
public class MessageInterceptor implements ChannelInterceptor {

    private final JWTManager jwtManager;
    private final GameService gameService;


    public MessageInterceptor(JWTManager jwtManager, GameService gameService) {
        this.jwtManager = jwtManager;
        this.gameService = gameService;
    }


    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        Object messageType = message.getHeaders().get("simpMessageType");
        if(messageType == SimpMessageType.DISCONNECT) { return message; }
        String authorization = accessor.getFirstNativeHeader("Authorization");


        int userId;

        try {
            userId = jwtManager.validateAndGetId(authorization.substring(7));
        } catch (JWTVerificationException
                 | NullPointerException
                 | StringIndexOutOfBoundsException e) { return null; } // null means cancel action


        String destination = accessor.getDestination();
        int gameId;

        try {
            int lastSlash = destination.lastIndexOf('/');
            gameId = Integer.parseInt(destination.substring(lastSlash + 1));
        } catch (NumberFormatException
                 | NullPointerException e) { return null; }


        if (gameId != gameService.getGameId(userId) || gameId == -1) {
            return null;
        }

        return message;
    }

}

