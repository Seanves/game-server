package net.seanv.stonegameserver.services;

import net.seanv.stonegameserver.entities.User;
import net.seanv.stonegameserver.dto.responses.Response;
import net.seanv.stonegameserver.dto.responses.UserStatus;
import net.seanv.stonegameserver.game.GameAcceptance;
import net.seanv.stonegameserver.game.GameQueue;
import net.seanv.stonegameserver.util.DeferredResultsHolder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class MatchmakingService {

    private final GameQueue queue;
    private final GameService gameService;
    private final Map<Integer, GameAcceptance> userIdAcceptanceMap;

    private final DeferredResultsHolder<Response> gameFoundWaitingResults;
    private final DeferredResultsHolder<Response> inAcceptanceWaitingResults;
    private final ScheduledExecutorService acceptanceDeleteExecutor;

    private final int ACCEPTANCE_TIMEOUT;


    public MatchmakingService(GameQueue queue,
                              GameService gameService,
                              @Value("${timeout.long_polling}") int defResTimeout,
                              @Value("${timeout.game_acceptance}") int acceptanceTimeout) {
        this.queue = queue;
        this.gameService = gameService;
        this.ACCEPTANCE_TIMEOUT = acceptanceTimeout;
        this.userIdAcceptanceMap = new ConcurrentHashMap<>();
        this.gameFoundWaitingResults = new DeferredResultsHolder<>(defResTimeout, Response.TIMEOUT);
        this.inAcceptanceWaitingResults = new DeferredResultsHolder<>(defResTimeout, Response.TIMEOUT);
        this.acceptanceDeleteExecutor = Executors.newSingleThreadScheduledExecutor();
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
            onGameFound(pair);
        }
        return Response.OK;
    }

    public boolean leaveQueue(int id) { return queue.remove(id); }

    public boolean isInQueue(int id) { return queue.contains(id); }

    private void createAcceptance(User user1, User user2) {
        GameAcceptance acceptance = new GameAcceptance(user1, user2);
        userIdAcceptanceMap.put(user1.getId(), acceptance);
        userIdAcceptanceMap.put(user2.getId(), acceptance);
        acceptanceDeleteExecutor.schedule(() -> deleteAcceptance(acceptance),
                                            ACCEPTANCE_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    private void deleteAcceptance(GameAcceptance acceptance) {
        userIdAcceptanceMap.remove(acceptance.getUser1().getId());
        userIdAcceptanceMap.remove(acceptance.getUser2().getId());
        onAcceptanceDelete(acceptance);
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

    private void onAcceptanceDelete(GameAcceptance acceptance) {
        int[] ids = { acceptance.getUser1().getId(), acceptance.getUser2().getId() };
        for (int id : ids) {
            inAcceptanceWaitingResults.complete(id, new Response(gameService.isInGame(id)));
        }
    }

    private void onGameFound(Pair<User,User> pair) {
        int[] ids = { pair.getFirst().getId(), pair.getSecond().getId() };
        for (int id : ids) {
            gameFoundWaitingResults.complete(id, Response.OK);
        }
    }

    public DeferredResult<Response> waitWhileInAcceptance(int userId) {
        if (!isInAcceptance(userId)) {
            return inAcceptanceWaitingResults.createAndSet(Response.NOT_IN_ACCEPTANCE);
        }

        return inAcceptanceWaitingResults.tryPut(userId, new Response(false));
    }

    public DeferredResult<Response> waitUntilGameFound(int userId) {
        if (!isInQueue(userId)) {
            return gameFoundWaitingResults.createAndSet(Response.NOT_IN_QUEUE);
        }

        return gameFoundWaitingResults.tryPut(userId, new Response(false));
    }


}
