package ru.max.test.junittest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Пример вложенных (context-based) тестов JUnit 5.
 * Идея:
 * - Внешний @BeforeEach init() создаёт новый пустой стек перед КАЖДЫМ тестом.
 * - @Nested-классы описывают разные контексты/состояния тестируемого объекта.
 * Контексты:
 * - WhenNew: проверки для нового стека (ожидаем пустоту).
 * - AfterPush: перед каждым тестом выполняется локальный @BeforeEach push(),
 * который кладёт 42 в стек; далее проверяем размер и значение pop().
 * Порядок инициализации для тестов внутри AfterPush:
 * 1) внешний @BeforeEach (init) -> создаёт пустой стек
 * 2) внутренний @BeforeEach (push) -> кладёт 42
 * Зачем @Nested:
 * - Делает предусловия и состояние явными (понятные "контексты").
 * - Улучшает читаемость и изоляцию: каждый тест запускается на свежем объекте.
 */

class StackNestedTest {
    Deque<Integer> stack;

    @BeforeEach
    void init() {
        stack = new ArrayDeque<>();
    }

    @Nested
    class WhenNew {
        @Test
        void empty() {
            assertTrue(stack.isEmpty());
        }
    }

    @Nested
    class AfterPush {
        @BeforeEach
        void push() {
            stack.push(42);
        }

        @Test
        void sizeIs1() {
            assertEquals(1, stack.size());
        }

        @Test
        void popReturn42() {
            assertEquals(42, stack.pop());
        }

    }


}
