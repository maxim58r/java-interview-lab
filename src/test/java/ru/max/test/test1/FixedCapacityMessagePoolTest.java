package ru.max.test.test1;


import org.junit.jupiter.api.Test;
import ru.max.test.config.Randoms;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FixedCapacityMessagePoolTest {
    @Test
    void add() {
        Message m = Randoms.a(Message.class);
        Message m1 = Randoms.a(Message.class);
        Message m2 = Randoms.a(Message.class);
        Message m3 = Randoms.a(Message.class);
        Message m4 = Randoms.a(Message.class);

        FixedCapacityMessagePool pool = new FixedCapacityMessagePool(3);
        pool.add(m);
        pool.add(m1);

        assertNull(pool.add(m2));
        assertEquals(m, pool.add(m3));
        assertEquals(m1, pool.add(m4));
        assertEquals(3, pool.size());
        assertNull(pool.add(m2));
        assertEquals(m3, pool.pollOldest());
    }

    @Test
    void addOne() {
        Message m = Randoms.a(Message.class);
        Message m1 = Randoms.a(Message.class);
        Message m2 = Randoms.a(Message.class);
        Message m3 = Randoms.a(Message.class);
        Message m4 = Randoms.a(Message.class);

        FixedCapacityMessagePool pool = new FixedCapacityMessagePool(1);

        assertNull(pool.add(m));
        assertEquals(m, pool.add(m1));
        assertEquals(m1, pool.add(m2));
        assertEquals(m2, pool.add(m3));
        assertEquals(m3, pool.add(m4));
        assertEquals(1, pool.size());
    }

    @Test
    void pollOldest() {
        Message m = Randoms.a(Message.class);
        Message m1 = Randoms.a(Message.class);

        FixedCapacityMessagePool pool = new FixedCapacityMessagePool(3);
        pool.add(m);
        pool.add(m1);

        assertEquals(m, pool.pollOldest());
        assertEquals(m1, pool.pollOldest());
        assertNull(pool.pollOldest());
        assertEquals(0, pool.size());
    }

    @Test
    void random() {
        int size = 5;
        FixedCapacityMessagePool pool = new FixedCapacityMessagePool(size);
        List<Message> many = Randoms.many(Message.class, size);

        assertNull(pool.random());

        many.forEach(pool::add);

        assertTrue(many.contains(pool.random()));

        pool.pollOldest();
        pool.pollOldest();
        pool.pollOldest();
        pool.pollOldest();
        pool.pollOldest();

        assertNull(pool.random());
    }

    @Test
    void removeById() {
        Message m = Randoms.a(Message.class);
        Message m1 = Randoms.a(Message.class);
        Message m2 = Randoms.a(Message.class);
        Message m3 = Randoms.a(Message.class);
        Message m4 = Randoms.a(Message.class);

        FixedCapacityMessagePool pool = new FixedCapacityMessagePool(5);
        pool.add(m);
        pool.add(m1);
        pool.add(m2);
        pool.add(m2);
        pool.add(m3);
        pool.add(m4);

        assertEquals(5, pool.size());
        assertNull(pool.removeById("09887"));
        assertEquals(m2, pool.removeById(m2.getId()));
        assertEquals(4, pool.size());
        assertNull(pool.removeById(""));
        assertNull(pool.removeById(null));
    }

    @Test
    void size() {
        int size = 5;
        FixedCapacityMessagePool pool = new FixedCapacityMessagePool(size);
        List<Message> many = Randoms.many(Message.class, size);

        many.forEach(pool::add);

        assertEquals(5, pool.size());
        pool.pollOldest();
        assertEquals(4, pool.size());
    }

    @Test
    void capacity() {
        int size = 5;
        FixedCapacityMessagePool pool = new FixedCapacityMessagePool(size);
        List<Message> many = Randoms.many(Message.class, size);

        many.forEach(pool::add);

        assertEquals(5, pool.size());
        assertEquals(5, pool.capacity());
        pool.pollOldest();
        assertEquals(4, pool.size());
        assertEquals(5, pool.capacity());
    }
}