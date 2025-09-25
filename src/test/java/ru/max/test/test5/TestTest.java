package ru.max.test.test5;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestTest {

    @Test
    void collapseArray() {
        ru.max.test.test5.Test test = new ru.max.test.test5.Test();

        String s = test.collapseArray(new int[]{1, 4, 5, 2, 3, 9, 8, 11, 0, 13});
        assertEquals("0-5,8-9,11,13", s);

        String s1 = test.collapseArray(new int[]{1});
        assertEquals("1", s1);

        String s2 = test.collapseArray(new int[]{});
        assertEquals("", s2);

        String s3 = test.collapseArray(new int[]{1, 13});
        assertEquals("1,13", s3);

        String s4 = test.collapseArray(null);
        assertEquals("", s4);

        String s5 = test.collapseArray(new int[]{1, 2, 2, 3});
        assertEquals("1-3", s5);

        String s6 = test.collapseArray(new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE + 1});
        assertEquals("-2147483648--2147483647", s6);
    }
}