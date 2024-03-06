package com.gameserver.game;

import org.springframework.stereotype.Component;
import java.util.*;

@Component
// thread safe, O(1) contains, deletes after timeout
public class GameQueue {

    private final Queue<Node> queue = new LinkedList<>();
    private final Map<Integer,Node> map = new HashMap<>();
    private final int TIMEOUT = 1000 * 30;

    {
        Thread timeoutThread = new Thread( () -> {
            while(true) {
                try { Thread.sleep(5000); } catch (InterruptedException e) { throw new RuntimeException(e); }
                synchronized(this) {
                    for(Node node: map.values()) {
                        if(System.currentTimeMillis() > node.time + TIMEOUT) {
                            remove(node);
                        }
                    }
                }
            }
        });
        timeoutThread.start();
    }


    public synchronized void add(int id) {
        Node node = new Node(id);
        if(map.containsKey(id)) { throw new AlreadyInQueueException(String.valueOf(id)); }
        queue.add(node);
        map.put(id, node);
    }

    public synchronized int poll() {
        Node polled = queue.poll();
        map.remove(polled.userId);
        return polled.userId;
    }

    // calls poll() 2 times to prevent other threads from changing queue between polls
    public synchronized int[] pollTwo() {
        if(this.size() >= 2) {
            return new int[]{poll(), poll()};
        }
        else { throw new RuntimeException("pollTwo() from size less than 2"); }
    }

    public synchronized void updateTime(int id) {
        Node node = map.get(id);
        if(node != null) { node.time = System.currentTimeMillis(); }
    }

    private synchronized void remove(Node node) {
        queue.remove(node);
        map.remove(node.userId);
    }

    public synchronized boolean remove(int id) {
        Node node = map.get(id);
        if(node != null) {
            queue.remove(node);
            map.remove(node.userId);
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

    @Override
    public String toString() {
        return queue + " " + map;
    }


    private static class Node {
        final int userId;
        Long time;

        Node(int userId) {
            this.userId = userId;
            this.time = System.currentTimeMillis();
        }

        @Override
        public boolean equals(Object o) {
            if(this == o) { return true; }
            if(o == null || getClass() != o.getClass()) { return false; }
            Node other = (Node) o;
            return userId == other.userId;
        }

        @Override
        public int hashCode() {
            return userId;
        }

        @Override
        public String toString() {
            return String.valueOf(userId);
        }
    }

    private static class AlreadyInQueueException extends RuntimeException {
        AlreadyInQueueException(String value) {
            super("value " + value);
        }
    }

}
