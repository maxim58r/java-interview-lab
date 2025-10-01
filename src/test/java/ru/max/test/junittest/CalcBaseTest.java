package ru.max.test.junittest;

import org.junit.jupiter.api.*;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Демонстрация жизненного цикла JUnit 5, порядка тестов и таймаутов.
 *
 * Аннотации:
 *  - @TestMethodOrder(OrderAnnotation.class): порядок задан через @Order.
 *  - @TestInstance(PER_CLASS): один экземпляр тестового класса на весь ран;
 *    позволяет делать @BeforeAll/@AfterAll НЕстатическими, но общий стейт (field state)
 *    разделяется между тестами — осторожно с параллельными запусками.
 *
 * Жизненный цикл:
 *  - @BeforeAll -> state = 0 (один раз перед всеми тестами)
 *  - @BeforeEach -> state++ (перед каждым тестом)
 *  - @AfterEach  -> state = 0 (сброс после каждого теста)
 *  - @AfterAll   -> state = 0 (финальная очистка)
 *
 * Таймауты:
 *  - @Timeout(1): глобальный лимит 1 секунда на ВЕСЬ метод timeoutOk().
 *  - assertTimeoutPreemptively(200ms): отдельный preemptive-лимит на конкретный блок;
 *    выполняется в другом потоке и прерывает по таймауту. Может быть нежелателен,
 *    если код зависит от текущего потока (ThreadLocal и т.п.).
 *
 * Рекомендации:
 *  - Избегать общего мутируемого состояния; если нужно — синхронизировать или
 *    использовать @TestInstance(PER_METHOD).
 *  - Не дублировать таймауты: оставить либо аннотационный (на весь метод),
 *    либо точечный (на конкретный блок).
 */

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CalcBaseTest {
    int state;

    @BeforeAll
    void setupAll() {
        state = 0;
    }

    @BeforeEach
    void setupEachAll() {
        state++;
    }

    @AfterEach
    void tearDown() {
        state = 0;
    }

    @AfterAll
    void done() {
        state = 0;
    }

    int add(int a, int b) {
        return a + b;
    }

    @Test
    @Order(1)
    void adds() {
        assertEquals(4, add(2, 2));
        assertTrue(state >= 1);
    }

    @Test
    @Order(2)
    @Timeout(1)
    void timeoutOk() {
        assertTimeoutPreemptively(Duration.ofMillis(200), () -> Thread.sleep(10));
    }
}
