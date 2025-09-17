package net.seanv.stonegameserver.websocket;

import net.seanv.stonegameserver.security.PrincipalImpl;
import net.seanv.stonegameserver.services.ChatService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import java.util.regex.Pattern;

@Component
public class ChatInterceptor implements ChannelInterceptor {
    private final ChatService chatService;
    private static final Pattern destinationPattern = Pattern.compile("^/chat/\\d+.*");

    public ChatInterceptor(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        String destination = accessor.getDestination();

        if (destination != null && destinationPattern.matcher(destination).matches()) {
            int chatId = Integer.parseInt(destination.split("/")[2]);
            int userId;

            if (accessor.getUser() instanceof PrincipalImpl p) {
                userId = p.getId();
            } else {
                throw new IllegalArgumentException("PrincipalImpl must be set before ChatInterceptor handling");
            }

            if (!chatService.isAccessPermitted(userId, chatId)) {
                return null;
            }
        }

        return message;
    }
}
