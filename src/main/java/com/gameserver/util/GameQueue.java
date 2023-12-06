package com.gameserver.util;

import lombok.Synchronized;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
// thread safe, O(1) contains, deletes after timeout
public class GameQueue {

    private final Queue<Node> queue = new LinkedList<>();
    private final Map<String,Node> map = new HashMap<>();
    private final int TIMEOUT = 1000 * 30;
    private final Object $lock = new Object();


    @Synchronized
    public void add(String ip) {
        Node node = new Node(ip);
        if(map.containsKey(node.value)) { throw new AlreadyInQueueException(node.value); }
        queue.add(node);
        map.put(node.value, node);
    }

    @Synchronized
    public String poll() {
        Node polled = queue.poll();
        map.remove(polled.value);
        return polled.value;
    }

    @Synchronized
    public String[] pollTwo() {
        if(this.size() >= 2) {
            return new String[]{poll(), poll()};
        }
        else { throw new RuntimeException("pollTwo() from size less than 2"); }
    }

    @Synchronized
    public String peek() {
        return queue.peek().value;
    }

    @Synchronized
    public void updateTime(String ip) {
        Node node = map.get(ip);
        if(node != null) { node.time = System.currentTimeMillis(); }
    }

    @Synchronized
    public void remove(Node node) {
        queue.remove(node);
        map.remove(node.value);
    }

    @Synchronized
    public boolean contains(String ip) {
        return map.containsKey(ip);
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
                            System.out.println("timeouted " + node.value);
                        }
                    }
                }
            }
        }).start();
    }


    private static class Node {
        final String value;
        Long time;

        Node(String value) {
            this.value = value;
            this.time = System.currentTimeMillis();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node other = (Node) o;
            return this.value.equals(other.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private static class AlreadyInQueueException extends RuntimeException {
        AlreadyInQueueException(String message) {
            super("value " + message);
        }
    }

}
