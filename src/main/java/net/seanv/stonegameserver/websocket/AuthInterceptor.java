package net.seanv.stonegameserver.websocket;

import com.auth0.jwt.exceptions.JWTVerificationException;
import net.seanv.stonegameserver.security.JWTManager;
import net.seanv.stonegameserver.security.PrincipalImpl;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;

@Component
public class AuthInterceptor implements ChannelInterceptor {
    private final JWTManager jwtManager;

    public AuthInterceptor(JWTManager jwtManager) {
        this.jwtManager = jwtManager;
    }


    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        String authorization = accessor.getFirstNativeHeader("Authorization");

        try {
            int userId = jwtManager.verifyAndGetId(authorization.substring(7));
            accessor.setUser(new PrincipalImpl(userId));
        } catch (JWTVerificationException
                 | NullPointerException
                 | StringIndexOutOfBoundsException e) {
            return errorFrame(accessor.getSessionId());
        }

        return message;
    }

    private Message<?> errorFrame(String sessionId) {
        StompHeaderAccessor errorAccessor = StompHeaderAccessor.create(StompCommand.ERROR);
        errorAccessor.setSessionId(sessionId);
        return MessageBuilder.createMessage(
                "Access denied".getBytes(StandardCharsets.UTF_8),
                errorAccessor.getMessageHeaders()
        );
    }

}
