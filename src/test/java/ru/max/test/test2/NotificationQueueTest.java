package ru.max.test.test2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

final class NotificationFactory {
    private static final AtomicInteger SEQ = new AtomicInteger();

    static Notification n(String prefix) {
        return new Notification(prefix + "-" + SEQ.getAndIncrement(), Instant.EPOCH);
    }
}

class NotificationQueueTest {

    @Test
    void fifoAddAndPopOrder_singleThread() {
        var q = new NotificationQueue();
        var n1 = NotificationFactory.n("A");
        var n2 = NotificationFactory.n("B");
        var n3 = NotificationFactory.n("C");

        q.addNotification(n1);
        q.addNotification(n2);
        q.addNotification(n3);

        assertEquals(3, q.size());
        assertSame(n1, q.popNotification());
        assertSame(n2, q.popNotification());
        assertSame(n3, q.popNotification());
        assertNull(q.popNotification());
        assertEquals(0, q.size());
    }

    @Test
    void getRandom_returnsOnlyExistingElements() {
        var q = new NotificationQueue();
        assertNull(q.randomNotification()); // пусто

        var a = NotificationFactory.n("A");
        var b = NotificationFactory.n("B");
        var c = NotificationFactory.n("C");
        q.addNotification(a);
        q.addNotification(b);
        q.addNotification(c);

        var allowed = Set.of(a, b, c);
        for (int i = 0; i < 1_000; i++) {
            var r = q.randomNotification();
            assertNotNull(r);
            assertTrue(allowed.contains(r));
        }

        assertSame(a, q.popNotification());
        for (int i = 0; i < 500; i++) {
            assertNotSame(a, q.randomNotification());
        }
    }

    @Test
    @Timeout(10)
        // чтобы не зависнуть при регрессии
    void concurrentAdders_onlyAdds_thenPopAll() throws Exception {
        var q = new NotificationQueue();

        int threads = 8, perThread = 5_000;
        int total = threads * perThread;

        var start = new CyclicBarrier(threads);
        ExecutorService es = Executors.newFixedThreadPool(threads);
        try {
            List<Future<?>> fs = new ArrayList<>();
            for (int t = 0; t < threads; t++) {
                fs.add(es.submit(() -> {
                    start.await();
                    for (int i = 0; i < perThread; i++) {
                        q.addNotification(NotificationFactory.n("X"));
                    }
                    return null;
                }));
            }
            for (var f : fs) f.get();

            assertEquals(total, q.size());

            // вытащим всё и убедимся, что ничего не потерялось/не дублируется
            int popped = 0;
            while (q.popNotification() != null) popped++;
            assertEquals(total, popped);
            assertEquals(0, q.size());
        } finally {
            es.shutdownNow();
        }
    }

    @Test
    @Timeout(15)
    void concurrentAddersAndPoppers_overlapWork() throws Exception {
        var q = new NotificationQueue();

        int producers = 4, consumers = 4, perProducer = 5_000;
        int total = producers * perProducer;

        ExecutorService es = Executors.newFixedThreadPool(producers + consumers);
        CountDownLatch producersDone = new CountDownLatch(producers);
        CyclicBarrier go = new CyclicBarrier(producers + consumers);
        AtomicInteger popped = new AtomicInteger();

        try {
            // producers
            for (int p = 0; p < producers; p++) {
                es.submit(() -> {
                    go.await();
                    for (int i = 0; i < perProducer; i++) {
                        q.addNotification(NotificationFactory.n("P"));
                    }
                    producersDone.countDown();
                    return null;
                });
            }
            // consumers
            for (int c = 0; c < consumers; c++) {
                es.submit(() -> {
                    go.await();
                    while (true) {
                        var n = q.popNotification();
                        if (n != null) {
                            if (popped.incrementAndGet() == total) break;
                        } else {
                            // если производители завершились и очередь пуста — выходим
                            if (producersDone.getCount() == 0 && q.size() == 0) break;
                            Thread.yield();
                        }
                    }
                    return null;
                });
            }

            es.shutdown();
            assertTrue(es.awaitTermination(12, TimeUnit.SECONDS), "Пулы не завершились вовремя");
            assertEquals(total, popped.get());
            assertEquals(0, q.size());
        } finally {
            es.shutdownNow();
        }
    }

    @Test
    @Timeout(10)
    void getRandom_isSafeUnderConcurrentAdds() throws Exception {
        var q = new NotificationQueue();

        ExecutorService es = Executors.newFixedThreadPool(3);
        try {
            Future<?> adder = es.submit(() -> {
                for (int i = 0; i < 10_000; i++) {
                    q.addNotification(NotificationFactory.n("R"));
                }
            });
            Future<?> reader = es.submit(() -> {
                for (int i = 0; i < 10_000; i++) {
                    var r = q.randomNotification(); // не должен кидать
                    if (r == null) {
                        // допустимо только если очередь моментально пуста в начале
                        // ничего не делаем
                    } else {
                        assertNotNull(r);
                    }
                }
            });
            adder.get();
            reader.get();

            // доберем всё, убедимся что состояние консистентно
            int rest = 0;
            while (q.popNotification() != null) rest++;
            assertTrue(rest >= 0);
        } finally {
            es.shutdownNow();
        }
    }
}
