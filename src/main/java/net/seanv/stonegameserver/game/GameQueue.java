package net.seanv.stonegameserver.game;

import net.seanv.stonegameserver.entities.User;
import lombok.EqualsAndHashCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;


@Component
// thread safe, O(1) contains, deletes after timeout
public class GameQueue {

    private final Queue<Node> queue = new LinkedList<>();
    private final Map<Integer,Node> map = new HashMap<>();
    private final int TIMEOUT;

    @EqualsAndHashCode
    private static class Node {
        final User user;
        @EqualsAndHashCode.Exclude
        Long time;

        Node(User user) {
            this.user = user;
            this.time = System.currentTimeMillis();
        }

        void updateTime() {
            time = System.currentTimeMillis();
        }

    }


    public GameQueue(@Value("${timeout.game_queue}") int timeout) {
        TIMEOUT = timeout;
    }


    @Scheduled(fixedDelayString = "#{${timeout.game_queue} / 3}")
    public synchronized void checkTimeout() {
        for (Node node: map.values()) {
            if (System.currentTimeMillis() > node.time + TIMEOUT) {
                remove(node);
            }
        }
    }

    public synchronized void add(User user) {
        Node node = new Node(user);
        if (map.containsKey(user.getId())) {
            throw new IllegalArgumentException("User " + user + " already in queue");
        }
        queue.add(node);
        map.put(user.getId(), node);
    }

    public synchronized User poll() {
        if (isEmpty()) { throw new IllegalStateException("poll from empty queue"); }
        Node polled = queue.poll();
        map.remove(polled.user.getId());
        return polled.user;
    }

    // encapsulate synchronization of different methods
    public synchronized Optional<Pair<User,User>> pollTwoIfPossible() {
        if (size() >= 2) {
            return Optional.of( Pair.of(poll(), poll()) );
        }
        return Optional.empty();
    }

    public synchronized void updateTime(int id) {
        Node node = map.get(id);
        if (node != null) { node.updateTime(); }
    }

    private synchronized void remove(Node node) {
        queue.remove(node);
        map.remove(node.user.getId());
    }

    public synchronized boolean remove(int id) {
        Node node = map.get(id);
        if (node != null) {
            queue.remove(node);
            map.remove(node.user.getId());
            return true;
        }
        return false;
    }

    public synchronized boolean contains(int id) {
        return map.containsKey(id);
    }

    public synchronized int size() {
        return queue.size();
    }

    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }

}
