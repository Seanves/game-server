package net.seanv.stonegameserver.websocket;

import com.auth0.jwt.exceptions.JWTVerificationException;
import net.seanv.stonegameserver.security.JWTManager;
import net.seanv.stonegameserver.services.GameService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import java.util.function.Function;
import java.util.regex.Pattern;

@Component
public class MessageInterceptor implements ChannelInterceptor {

    private final JWTManager jwtManager;
    private final Function<Integer,Integer> getGameId;
    private final Pattern destinationPattern;


    public MessageInterceptor(JWTManager jwtManager, GameService gameService) {
        this.jwtManager = jwtManager;
        getGameId = gameService::getGameId;
        destinationPattern = Pattern.compile("^/room/\\d+$");
    }


    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        Object messageType = message.getHeaders().get("simpMessageType");
        String authorization = accessor.getFirstNativeHeader("Authorization");


        int userId;

        // verify authentication
        try {
            userId = jwtManager.verifyAndGetId(authorization.substring(7));
        } catch (JWTVerificationException
                 | NullPointerException
                 | StringIndexOutOfBoundsException e) { return null; } // null means cancel action


        if (messageType != SimpMessageType.CONNECT) {
            String destination = accessor.getDestination();
            int gameId;

            // check path
            if (destination == null || !destinationPattern.matcher(destination).matches()) {
                return null;
            }

            // check is user in game with the same id as room
            try {
                int lastSlash = destination.lastIndexOf('/');
                gameId = Integer.parseInt(destination.substring(lastSlash + 1));
            } catch (NumberFormatException e) { return null; }


            if (gameId != getGameId.apply(userId) || gameId == -1) {
                return null;
            }
        }

        return message;
    }

}

