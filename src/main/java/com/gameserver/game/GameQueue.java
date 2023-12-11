package com.gameserver.game;

import org.springframework.stereotype.Component;
import java.util.*;
import com.gameserver.entities.User;

@Component
// thread safe, O(1) contains, deletes after timeout
public class GameQueue {

    private final Queue<Node> queue = new LinkedList<>();
    private final Map<User,Node> map = new HashMap<>();
    private final int TIMEOUT = 1000 * 30;


    public synchronized void add(User user) {
        Node node = new Node(user);
        if(map.containsKey(user)) { throw new AlreadyInQueueException(String.valueOf(node.user)); }
        queue.add(node);
        map.put(user, node);
    }

    public synchronized User poll() {
        Node polled = queue.poll();
        map.remove(polled.user);
        return polled.user;
    }

    public synchronized User[] pollTwo() {
        if(this.size() >= 2) {
            return new User[]{poll(), poll()};
        }
        else { throw new RuntimeException("pollTwo() from size less than 2"); }
    }

    public synchronized User peek() {
        return queue.peek().user;
    }

    public synchronized void updateTime(User user) {
        Node node = map.get(user);
        if(node != null) { node.time = System.currentTimeMillis(); }
    }

    public synchronized void remove(Node node) {
        queue.remove(node);
        map.remove(node.user);
    }

    public synchronized boolean remove(User user) {
        Node node = map.get(user);
        if(node != null) {
            queue.remove(node);
            map.remove(node.user);
            return true;
        }
        return false;
    }

    public synchronized boolean contains(User user) {
        return map.containsKey(user);
    }

    public int size() {
        return queue.size();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public String toString() {
        return queue.toString() + " " + map.toString();
    }


    /* initialization */ {
        new Thread( () -> {
            while(true) {
                synchronized(this) {
                    try { wait(1000); } catch (InterruptedException e) { throw new RuntimeException(e); }
                    for(Node node: map.values()) {
                        if(System.currentTimeMillis() > node.time + TIMEOUT) {
                            remove(node);
                            System.out.println("timeouted " + node.user.getId());
                        }
                    }
                }
            }
        }).start();
    }


    private static class Node {
        final User user;
        Long time;

        Node(User user) {
            this.user = user;
            this.time = System.currentTimeMillis();
        }

        @Override
        public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            Node other = (Node) o;
            return this.user.equals(other.user);
        }

        @Override
        public int hashCode() {
            return this.user.hashCode();
        }

        @Override
        public String toString() {
            return String.valueOf(user.getId());
        }
    }

    private static class AlreadyInQueueException extends RuntimeException {
        AlreadyInQueueException(String value) {
            super("value " + value);
        }
    }

}
