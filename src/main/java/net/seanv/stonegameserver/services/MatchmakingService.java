package net.seanv.stonegameserver.services;

import net.seanv.stonegameserver.entities.User;
import net.seanv.stonegameserver.dto.responses.Response;
import net.seanv.stonegameserver.dto.responses.UserStatus;
import net.seanv.stonegameserver.game.GameAcceptance;
import net.seanv.stonegameserver.game.GameQueue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MatchmakingService {

    private final GameQueue queue;
    private final Map<Integer, GameAcceptance> userIdAcceptanceMap;
    private final GameService gameService;

    private final long NOTIFYING_TIMEOUT;


    public MatchmakingService(GameQueue queue, GameService gameService,
                              @Value("${NOTIFYING_TIMEOUT}") long timeout) {
        this.queue = queue;
        this.userIdAcceptanceMap = new ConcurrentHashMap<>();
        this.gameService = gameService;
        this.NOTIFYING_TIMEOUT = timeout;
    }


    public Response addInQueue(User user) {
        if (isInQueue(user.getId()))            { return Response.ALREADY_IN_QUEUE; }
        if (isInAcceptance(user.getId()))       { return Response.ALREADY_IN_ACCEPTANCE; }
        if (gameService.isInGame(user.getId())) { return Response.ALREADY_IN_GAME; }

        queue.add(user);

        Optional<Pair<User,User>> optionalPair = queue.pollTwoIfPossible();

        if (optionalPair.isPresent()) {
            Pair<User,User> pair = optionalPair.get();
            createAcceptance(pair.getFirst(), pair.getSecond());
        }
        return Response.OK;
    }

    public boolean leaveQueue(int id) { return queue.remove(id); }

    public boolean isInQueue(int id) { return queue.contains(id); }

    private void createAcceptance(User user1, User user2) {
        GameAcceptance acceptance = new GameAcceptance(user1, user2, this::deleteAcceptance);
        userIdAcceptanceMap.put(user1.getId(), acceptance);
        userIdAcceptanceMap.put(user2.getId(), acceptance);
    }

    private void deleteAcceptance(GameAcceptance acceptance) {
        userIdAcceptanceMap.remove(acceptance.getUser1().getId());
        userIdAcceptanceMap.remove(acceptance.getUser2().getId());
    }

    public boolean isInAcceptance(int id) {
        return userIdAcceptanceMap.containsKey(id);
    }

    public boolean acceptGame(int userId) {
        GameAcceptance acceptance = userIdAcceptanceMap.get(userId);
        if (acceptance == null) { return false; }
        acceptance.accept(userId);

        if (acceptance.isEverybodyAccepted()) {
            gameService.createGameSession(acceptance.getUser1(), acceptance.getUser2());
            deleteAcceptance(acceptance);
        }
        return true;
    }

    public boolean declineGame(int id) {
        GameAcceptance acceptance = userIdAcceptanceMap.get(id);
        if (acceptance == null) { return false; }

//        acceptance.decline(id);
        deleteAcceptance(acceptance);
        return true;
    }

    public void updateTime(int id) { queue.updateTime(id); }

    public UserStatus getUserStatus(int id) {
        updateTime(id);
        return new UserStatus(isInQueue(id), isInAcceptance(id), gameService.isInGame(id));
    }


    public DeferredResult<Response> waitUntilGameFound(int userId) {
        DeferredResult<Response> deferredResult = new DeferredResult<>(NOTIFYING_TIMEOUT, new Response(false, "timeout"));
        CompletableFuture.runAsync( () -> {
            while (isInQueue(userId)) {
                try { Thread.sleep(500); } catch (Exception e) { throw new RuntimeException(e); }
            }
            deferredResult.setResult(new Response(isInAcceptance(userId)));
        });
        return deferredResult;
    }

    public DeferredResult<Response> waitWhileInAcceptance(int userId) {
        DeferredResult<Response> deferredResult = new DeferredResult<>(NOTIFYING_TIMEOUT, new Response(false, "timeout"));
        CompletableFuture.runAsync( () -> {
            while (isInAcceptance(userId)) {
                try { Thread.sleep(500); } catch (Exception e) { throw new RuntimeException(e); }
            }
            deferredResult.setResult(new Response(gameService.isInGame(userId)));
        });
        return deferredResult;
    }

}
