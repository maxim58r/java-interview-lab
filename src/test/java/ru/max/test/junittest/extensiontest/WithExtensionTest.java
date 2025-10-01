package ru.max.test.junittest.extensiontest;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Пример JUnit 5 Extension: измерение длительности теста.
 *
 * Как работает:
 *  - TimingExtension реализует BeforeTestExecutionCallback и AfterTestExecutionCallback.
 *  - Перед тестом сохраняем в ExtensionContext.Store текущее время (System.nanoTime()).
 *  - После теста достаём метку, считаем прошедшее время и печатаем "[TIMING] <имя> took <ms>ms".
 *
 * Зачем:
 *  - Лёгкий способ логировать время выполнения каждого теста, не меняя тела тестов.
 *
 * Подключение:
 *  - На тестовом классе: @ExtendWith(TimingExtension.class).
 *
 * Примечание:
 *  - Для изоляции лучше использовать store-namespace на уровне конкретного теста
 *    (см. улучшенную версию TimingExtension ниже), чтобы избежать конфликтов в параллельных запусках.
 */

@ExtendWith(TimingExtension.class)
class WithExtensionTest {
    @Test
    void demo() throws Exception {
        Thread.sleep(10);
        Assertions.assertTrue(true);
    }
}