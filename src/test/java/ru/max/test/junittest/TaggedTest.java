package ru.max.test.junittest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Пример использования тегов JUnit 5.
 * Идея:
 *  - @Tag("fast") и @Tag("slow") помечают тесты как принадлежащие группам.
 *  - Теги позволяют фильтровать тесты при запуске (в Gradle/Maven/IDE).
 * Применение:
 *  - Разделять быстрые и медленные тесты, юнит и интеграционные, e2e и т.п.
 *  - В CI можно запускать только fast на каждый коммит, а slow — по расписанию.
 * См. build.gradle:
 *  tasks.withType(Test).configureEach {
 *    useJUnitPlatform {
 *      includeTags("fast")
 *      excludeTags("slow")
 *    }
 *  }
 */

class TaggedTest {
    @Test
    @Tag("fast")
    void fast() {
        Assertions.assertTrue(true);
    }

    @Test
    @Tag("slow")
    void slow() {
        Assertions.assertTrue(true);
    }
}
