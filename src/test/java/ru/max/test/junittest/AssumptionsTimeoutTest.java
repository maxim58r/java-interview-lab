package ru.max.test.junittest;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Демонстрация предпосылок (Assumptions) и таймаутов в JUnit 5.
 *
 * assumes():
 *  - assumeTrue(...) задаёт УСЛОВИЕ выполнения теста.
 *  - Если условие ложно — тест ПОМЕЧАЕТСЯ как skipped (пропущен), а не failed.
 *  - Здесь тест выполняется только если задано системное свойство "os.name".
 *
 * timeout():
 *  - assertTimeout(Duration, Executable) проверяет, что код укладывается в заданный лимит.
 *  - Если время превышено — тест падает с AssertionFailedError.
 *  - Вариант assertTimeout НЕ прерывает выполнение кода (не preemptive);
 *    если нужна принудительная прерываемость — используйте assertTimeoutPreemptively(...).
 *
 * Назначение:
 *  - Assumptions подходят для окружение-зависимых проверок (ОС, ENV, CI и т.д.).
 *  - Таймауты защищают от подвисаний и регрессий по производительности в тестах.
 */
class AssumptionsTimeoutTest {
    @Test
    void assumes() {
        assumeTrue(System.getProperty("os.name") != null);
        assertEquals(9, 3 * 3);
    }

    @Test
    void timeout() {
        assertTimeout(Duration.ofMillis(100), () -> Thread.sleep(10));
    }
}
