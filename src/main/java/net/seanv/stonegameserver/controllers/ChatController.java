package net.seanv.stonegameserver.controllers;

import net.seanv.stonegameserver.dto.ChatMessage;
import net.seanv.stonegameserver.security.PrincipalImpl;
import net.seanv.stonegameserver.services.ChatService;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @MessageMapping("/chat/{id}/send")
    @SendTo("/chat/{id}/receive")
    @PreAuthorize("@chatService.isAccessPermitted(#principal.id, #id)")
    public ChatMessage sendMessage(@DestinationVariable int id,
                                   @Payload String text,
                                   PrincipalImpl principal) {

        return chatService.processMessage(text, principal.getId());
    }

    @MessageExceptionHandler(AccessDeniedException.class)
    public void exceptionHandle() {
    }

}