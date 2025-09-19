package ru.max.test.test1;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Потокобезопасный пул фиксированной ёмкости.
 * Семантика:
 * - add(id) при повторном id обновляет объект и переносит его в хвост FIFO (refresh).
 * - При переполнении вытесняется самый старый.
 * - random() возвращает случайный текущий элемент или null, если пусто.
 * <p>
 * Требует JDK 21+ (SequencedMap/SequencedCollection).
 */
public class FixedCapacityMessagePool {

    private final LinkedHashMap<String, Message> fifo; // insertion-order
    private final Map<String, Integer> pos;            // id -> index in bag
    private final List<Message> bag;                   // плотный массив
    private final int capacity;
    private final ReentrantLock lock = new ReentrantLock();

    public FixedCapacityMessagePool(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be > 0, got " + capacity);
        }
        this.capacity = capacity;
        this.fifo = new LinkedHashMap<>(capacity, 1.0f); // мы жёстко держим размер
        this.pos = new HashMap<>(capacity, 1.0f);
        this.bag = new ArrayList<>(capacity);
    }

    /**
     * Добавляет сообщение. При переполнении возвращает вытеснённое, иначе null.
     */
    public Message add(Message m) {
        if (m == null) throw new IllegalArgumentException("message must not be null");
        lock.lock();
        try {
            final String id = m.getId();

            // refresh существующего: переносим в хвост FIFO и заменяем в bag по месту
            Message existed = fifo.remove(id);
            if (existed != null) {
                int idx = requireIndex(id, "add-refresh");
                placeAtIdx(idx, m);      // заменить объект по текущему индексу
                fifo.putLast(id, m);     // в хвост
                devCheckInvariant();
                return null;
            }

            // просто вместилось
            if (fifo.size() < capacity) {
                append(m);
                devCheckInvariant();
                return null;
            }

            // переполнение: вытесняем голову
            Message evicted = fifo.pollFirstEntry().getValue();
            int idx = requireIndex(evicted.getId(), "add-evict");

// УДАЛЯЕМ старую запись обязательно:
            pos.remove(evicted.getId());

            int last = bag.size() - 1;
            if (idx != last) {
                Message tail = bag.get(last);
                placeAtIdx(idx, tail); // tail -> idx, pos обновили внутри
            }
// в последнюю ячейку кладём новый элемент (без изменения размера bag)
            bag.set(last, m);
            pos.put(m.getId(), last);
            fifo.putLast(m.getId(), m);

            devCheckInvariant();
            return evicted;

        } finally {
            lock.unlock();
        }
    }

    /**
     * Возвращает и удаляет самый старый элемент, или null если пусто.
     */
    public Message pollOldest() {
        lock.lock();
        try {
            var e = fifo.pollFirstEntry();
            if (e == null) return null;
            return removeFromStructures(e.getValue(), "pollOldest");
        } finally {
            lock.unlock();
        }
    }

    /**
     * Удаляет элемент по id за O(1). Возвращает удалённый или null.
     */
    public Message removeById(String id) {
        if (id == null || id.isBlank()) return null;
        lock.lock();
        try {
            Message removed = fifo.remove(id);
            if (removed == null) return null;
            return removeFromStructures(removed, "removeById");
        } finally {
            lock.unlock();
        }
    }

    /**
     * Случайный текущий элемент или null, если пусто.
     */
    public Message random() {
        lock.lock();
        try {
            int n = bag.size();
            if (n == 0) return null;
            return bag.get(ThreadLocalRandom.current().nextInt(n));
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        lock.lock();
        try {
            return bag.size();
        } finally {
            lock.unlock();
        }
    }

    public int capacity() {
        return capacity;
    }

    // ---------- внутренние хелперы (lock уже удерживается) ----------

    /**
     * Положить элемент m в конец всех структур.
     */
    private void append(Message m) {
        fifo.putLast(m.getId(), m);
        bag.addLast(m);
        pos.put(m.getId(), bag.size() - 1);
    }

    /**
     * Положить элемент m на индекс idx (обновляет pos).
     */
    private void placeAtIdx(int idx, Message m) {
        bag.set(idx, m);
        pos.put(m.getId(), idx);
    }

    /**
     * Удалить элемент из bag/pos за O(1) и вернуть его.
     */
    private Message removeFromStructures(Message removed, String op) {
        int idx = requireIndex(removed.getId(), op);
        int last = bag.size() - 1;

        if (idx != last) {
            Message tail = bag.get(last);
            placeAtIdx(idx, tail);
        }
        // срезаем хвост
        bag.remove(last);
        pos.remove(removed.getId()); // на всякий случай (idempotent)

        devCheckInvariant();
        return removed;
    }

    /**
     * Достаёт индекс из pos или кидает осмысленное исключение (внутренний инвариант).
     */
    private int requireIndex(String id, String op) {
        Integer idx = pos.get(id);
        if (idx == null) {
            throw new IllegalStateException("pos index missing for id=" + id + " in " + op);
        }
        return idx;
    }

    /**
     * Глубокая проверка инвариантов (в dev-сборке можно повесить на assert/флаг).
     */
    private void devCheckInvariant() {
        int size = bag.size();
        if (size != pos.size() || size != fifo.size()) {
            throw new IllegalStateException("sizes differ: bag=" + bag.size() +
                    " pos=" + pos.size() + " fifo=" + fifo.size());
        }
        // глубокая проверка соответствия индексов
        for (int i = 0; i < bag.size(); i++) {
            Message m = bag.get(i);
            Integer pi = pos.get(m.getId());
            if (pi == null || pi != i) {
                throw new IllegalStateException("pos mismatch for id=" + m.getId() +
                        " bagIndex=" + i + " posIndex=" + pi);
            }
        }
        // ключи fifo == ключи pos
        if (!fifo.keySet().equals(pos.keySet())) {
            throw new IllegalStateException("key sets differ: fifo=" + fifo.keySet() +
                    " pos=" + pos.keySet());
        }
    }
}

