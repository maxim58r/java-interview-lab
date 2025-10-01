package ru.max.test.junittest;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Демонстрация динамических тестов JUnit 5.
 * Идея: метод с @TestFactory не запускает тест сам, а ВОЗВРАЩАЕТ набор тестов,
 * которые JUnit создаёт и выполняет во время выполнения (runtime).
 * В данном примере:
 * - Из списка чисел [1,2,3,5,8] программно формируются 5 тестов.
 * - Имя каждого теста: "n=<число>".
 * - Проверка для каждого: число > 0.
 * Что важно знать:
 * - @TestFactory ≠ @Test: метод должен возвращать Stream/Collection/List<DynamicTest>.
 * - Каждый DynamicTest отображается и считается отдельным тест-кейсом в отчёте.
 * - Подходит, когда набор кейсов вычисляется из данных (файлы, БД, конфиг и т.п.).
 */
class DynamicDemoTest {
    @TestFactory
    List<DynamicTest> generate() {
        return Stream.of(1, 2, 3, 5, 8)
                .map(n -> DynamicTest.dynamicTest("n=" + n, () -> assertTrue(n > 0)))
                .toList();
    }
}
