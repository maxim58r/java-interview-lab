package ru.max.test.test1;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class FixedCapacityMessagePoolFuzzTest {

    @Test
    @Timeout(20) // чтобы не зависнуть при регрессии
    void longRandomScenario() {
        final int capacity = 64;
        final int steps = 100_000;

        var pool = new FixedCapacityMessagePool(capacity);
        var model = new ReferencePool(capacity);

        // фиксируем seed для детерминизма
        var rnd = new Random(42);

        // Вселенная возможных id: часть берём из существующих, часть — новые
        int idSeq = 0;

        for (int step = 0; step < steps; step++) {
            double p = rnd.nextDouble();

            if (p < 0.60) { // add
                boolean reuseExisting = rnd.nextDouble() < 0.25 && !model.isEmpty();
                String id = reuseExisting ? model.randomId(rnd) : "id-" + (idSeq++);
                Message m = new Message(id, Instant.now().toEpochMilli(), "t" + step);

                Message evictedPool  = pool.add(m);
                Message evictedModel = model.add(m);

                assertEquals(evictedModel, evictedPool, "evicted differs at step " + step);

            } else if (p < 0.80) { // removeById
                String id;
                // с вероятностью 70% удаляем существующий id, иначе — несуществующий
                if (rnd.nextDouble() < 0.70 && !model.isEmpty()) {
                    id = model.randomId(rnd);
                } else {
                    id = "unknown-" + rnd.nextInt(1_000_000);
                }

                Message removedPool  = pool.removeById(id);
                Message removedModel = model.removeById(id);

                assertEquals(removedModel, removedPool, "removeById mismatch at step " + step);

            } else { // pollOldest
                Message polledPool  = pool.pollOldest();
                Message polledModel = model.pollOldest();

                assertEquals(polledModel, polledPool, "pollOldest differs at step " + step);
            }

            // 1) размеры должны совпадать
            assertEquals(model.size(), pool.size(), "size mismatch at step " + step);

            // 2) random() должен быть либо null (когда пусто), либо id из набора модели
            Message r = pool.random();
            if (model.isEmpty()) {
                assertNull(r, "random() must be null on empty at step " + step);
            } else {
                assertNotNull(r, "random() returned null on non-empty at step " + step);
                assertTrue(model.containsId(r.getId()),
                        "random() returned id not present in FIFO at step " + step);
            }
        }
    }

    /**
     * Эталонная «модель» поведения пула:
     * - FIFO на LinkedHashMap (insertion-order),
     * - при повторном id: обновляем сообщение и переносим его в хвост,
     * - при переполнении: эвиктим самое старое (голову).
     */
    private static final class ReferencePool {
        private final int capacity;
        private final LinkedHashMap<String, Message> fifo =
                new LinkedHashMap<>(128, 0.75f, false); // insertion-order

        ReferencePool(int capacity) { this.capacity = capacity; }

        Message add(Message m) {
            Objects.requireNonNull(m);
            String id = m.getId();
            if (fifo.containsKey(id)) {
                // refresh: перенести в хвост и обновить значение
                fifo.remove(id);
                fifo.put(id, m);
                return null;
            }
            if (fifo.size() < capacity) {
                fifo.put(id, m);
                return null;
            }
            // эвикт головы
            var it = fifo.entrySet().iterator();
            Map.Entry<String, Message> eldest = it.next();
            it.remove();
            Message evicted = eldest.getValue();
            fifo.put(id, m);
            return evicted;
        }

        Message removeById(String id) {
            if (id == null || id.isBlank()) return null;
            return fifo.remove(id);
        }

        Message pollOldest() {
            var it = fifo.entrySet().iterator();
            if (!it.hasNext()) return null;
            Map.Entry<String, Message> eldest = it.next();
            it.remove();
            return eldest.getValue();
        }

        int size() { return fifo.size(); }
        boolean isEmpty() { return fifo.isEmpty(); }
        boolean containsId(String id) { return fifo.containsKey(id); }

        String randomId(Random rnd) {
            int n = fifo.size();
            int target = rnd.nextInt(n); // 0..n-1
            int i = 0;
            for (String id : fifo.keySet()) {
                if (i++ == target) return id;
            }
            throw new AssertionError("unreachable");
        }
    }
}
