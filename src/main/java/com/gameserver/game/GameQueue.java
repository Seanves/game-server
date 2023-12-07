package com.gameserver.game;

import lombok.Synchronized;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
// thread safe, O(1) contains, deletes after timeout
public class GameQueue {

    private final Queue<Node> queue = new LinkedList<>();
    private final Map<Integer,Node> map = new HashMap<>();
    private final int TIMEOUT = 1000 * 30;
    private final Object $lock = new Object();


    @Synchronized
    public void add(int id) {
        Node node = new Node(id);
        if(map.containsKey(node.id)) { throw new AlreadyInQueueException(String.valueOf(node.id)); }
        queue.add(node);
        map.put(node.id, node);
    }

    @Synchronized
    public int poll() {
        Node polled = queue.poll();
        map.remove(polled.id);
        return polled.id;
    }

    @Synchronized
    public int[] pollTwo() {
        if(this.size() >= 2) {
            return new int[]{poll(), poll()};
        }
        else { throw new RuntimeException("pollTwo() from size less than 2"); }
    }

    @Synchronized
    public int peek() {
        return queue.peek().id;
    }

    @Synchronized
    public void updateTime(int id) {
        Node node = map.get(id);
        if(node != null) { node.time = System.currentTimeMillis(); }
    }

    @Synchronized
    public void remove(Node node) {
        queue.remove(node);
        map.remove(node.id);
    }

    @Synchronized
    public boolean remove(int id) {
        Node node = map.get(id);
        if(node != null) {
            queue.remove(node);
            map.remove(node.id);
            return true;
        }
        return false;
    }

    @Synchronized
    public boolean contains(int id) {
        return map.containsKey(id);
    }

    public int size() {
        return queue.size();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override @Synchronized
    public String toString() {
        return queue.toString() + " " + map.toString();
    }


    /* initialization */ {
        new Thread( () -> {
            while(true) {
                synchronized($lock) {
                    try { $lock.wait(1000); } catch (InterruptedException e) { throw new RuntimeException(e); }
                    for(Node node: map.values()) {
                        if(System.currentTimeMillis() > node.time + TIMEOUT) {
                            remove(node);
                            System.out.println("timeouted " + node.id);
                        }
                    }
                }
            }
        }).start();
    }


    private static class Node {
        final int id;
        Long time;

        Node(int id) {
            this.id = id;
            this.time = System.currentTimeMillis();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node other = (Node) o;
            return this.id == other.id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public String toString() {
            return String.valueOf(id);
        }
    }

    private static class AlreadyInQueueException extends RuntimeException {
        AlreadyInQueueException(String message) {
            super("value " + message);
        }
    }

}
