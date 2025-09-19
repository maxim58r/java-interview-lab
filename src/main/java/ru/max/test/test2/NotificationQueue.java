package ru.max.test.test2;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantLock;

public final class NotificationQueue {
    private final ArrayDeque<Notification> fifo = new ArrayDeque<>();
    private final ArrayList<Notification> bag = new ArrayList<>();
    private final IdentityHashMap<Notification, Integer> pos = new IdentityHashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    public void addNotification(Notification n) {
        lock.lock();
        try {
            fifo.addLast(n);
            pos.put(n, bag.size());
            bag.add(n);
        } finally {
            lock.unlock();
        }
    }

    public Notification popNotification() {
        lock.lock();
        try {
            Notification n = fifo.pollFirst();
            if (n == null) return null;

            Integer iObj = pos.remove(n);
            if (iObj != null) {
                int i = iObj;
                int last = bag.size() - 1;
                if (i != last) {
                    Notification tail = bag.get(last);
                    bag.set(i, tail);
                    pos.put(tail, iObj);
                }
                bag.remove(last);
            }
            return n;
        } finally {
            lock.unlock();
        }
    }

    public Notification randomNotification() {
        lock.lock();
        try {
            if (bag.isEmpty()) return null;
            int idx = ThreadLocalRandom.current().nextInt(bag.size());
            return bag.get(idx);
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        lock.lock();
        try {
            return fifo.size();
        } finally {
            lock.unlock();
        }
    }
}
