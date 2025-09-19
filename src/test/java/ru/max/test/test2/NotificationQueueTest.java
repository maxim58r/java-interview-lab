package ru.max.test.test2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import ru.max.test.config.Randoms;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.locks.LockSupport.parkNanos;
import static org.junit.jupiter.api.Assertions.*;

class NotificationQueueTest {

    @Test
    void fifoAddAndPopOrder_singleThread() {
        var q = new NotificationQueue();
        var n1 = Randoms.a(Notification.class);
        var n2 = Randoms.a(Notification.class);
        var n3 = Randoms.a(Notification.class);

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

        var a = Randoms.a(Notification.class);
        var b = Randoms.a(Notification.class);
        var c = Randoms.a(Notification.class);
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
    void concurrentAdders_onlyAdds_thenPopAll() throws Exception {
        var q = new NotificationQueue();

        int threads = 8, perThread = 5_000;
        int total = threads * perThread;

        try (ExecutorService es = Executors.newFixedThreadPool(threads)) {
            var start = new CyclicBarrier(threads);
            List<Future<?>> fs = new ArrayList<>();

            for (int t = 0; t < threads; t++) {
                fs.add(es.submit(() -> {
                    start.await();
                    for (int i = 0; i < perThread; i++) {
                        q.addNotification(Randoms.a(Notification.class));
                    }
                    return null;
                }));
            }

            // поднимем ошибки из потоков
            for (var f : fs) f.get();

            assertEquals(total, q.size());

            int popped = 0;
            while (q.popNotification() != null) popped++;
            assertEquals(total, popped);
            assertEquals(0, q.size());
        }
    }

    @Test
    @Timeout(15)
    void concurrentAddersAndPoppers_overlapWork() throws Exception {
        var q = new NotificationQueue();

        int producers = 4, consumers = 4, perProducer = 5_000;
        int total = producers * perProducer;

        try (ExecutorService es = Executors.newFixedThreadPool(producers + consumers)) {
            CountDownLatch producersDone = new CountDownLatch(producers);
            CyclicBarrier go = new CyclicBarrier(producers + consumers);
            AtomicInteger popped = new AtomicInteger();
            List<Future<?>> fs = new ArrayList<>();

            // producers
            for (int p = 0; p < producers; p++) {
                fs.add(es.submit(() -> {
                    try {
                        go.await();
                        for (int i = 0; i < perProducer; i++) {
                            q.addNotification(Randoms.a(Notification.class));
                        }
                    } finally {
                        producersDone.countDown(); // считаем всегда, даже если поток упал
                    }
                    return null;
                }));
            }

            // consumers
            for (int c = 0; c < consumers; c++) {
                fs.add(es.submit(() -> {
                    go.await();
                    while (true) {
                        var n = q.popNotification();
                        if (n != null) {
                            if (popped.incrementAndGet() == total) break;
                        } else {
                            if (producersDone.getCount() == 0 && q.size() == 0) break;
                            // чуть отпустим CPU
                            parkNanos(1_000_000);
                        }
                    }
                    return null;
                }));
            }

            es.shutdown();

            // если внутри были исключения — тест упадёт здесь
            for (var f : fs) f.get();

            assertTrue(es.awaitTermination(12, TimeUnit.SECONDS), "Пулы не завершились вовремя");
            assertEquals(total, popped.get());
            assertEquals(0, q.size());
        }
    }

    @Test
    @Timeout(10)
    void getRandom_isSafeUnderConcurrentAdds() throws Exception {
        var q = new NotificationQueue();

        try (ExecutorService es = Executors.newFixedThreadPool(3)) {
            Future<?> adder = es.submit(() -> {
                for (int i = 0; i < 10_000; i++) {
                    q.addNotification(Randoms.a(Notification.class));
                }
                return null;
            });
            Future<?> reader = es.submit(() -> {
                for (int i = 0; i < 10_000; i++) {
                    // не должен кидать и, если не пусто, не должен быть null
                    var r = q.randomNotification();
                    if (q.size() > 0) {
                        assertNotNull(r);
                    }
                }
                return null;
            });

            adder.get();
            reader.get();

            // доберём всё, убеждаемся в консистентности
            int rest = 0;
            while (q.popNotification() != null) rest++;
            assertTrue(rest >= 0);
            assertEquals(0, q.size());
        }
    }
}
