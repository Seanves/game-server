package net.seanv.stonegameserver.services;

import net.seanv.stonegameserver.dto.ChatMessage;
import net.seanv.stonegameserver.repositories.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class ChatService {
    private final UserRepository userRepository;
    private final GameService gameService;

    public ChatService(UserRepository userRepository, GameService gameService) {
        this.userRepository = userRepository;
        this.gameService = gameService;
    }

    public boolean isAccessPermitted(int userId, int chatId) {
        return chatId != -1 && chatId == gameService.getGameId(userId);
    }

    public ChatMessage processMessage(String text, int userId) {
        String nickname = userRepository.findNicknameById(userId).orElseThrow(
                            () -> new IllegalArgumentException("userId " + userId));

        return new ChatMessage(nickname, text);
    }

}