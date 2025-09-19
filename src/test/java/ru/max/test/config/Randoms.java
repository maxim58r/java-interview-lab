package ru.max.test.config;


import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;

import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;

public final class Randoms {
    private Randoms() {}

    // Базовые параметры: повторяемость, адекватные размеры, перезапись дефолтных полей
    private static final EasyRandomParameters P = new EasyRandomParameters()
            .seed(2025_09_17L)                 // воспроизводимость между запусками
            .collectionSizeRange(1, 3)
            .stringLengthRange(3, 24)
            .charset(StandardCharsets.UTF_8)
            .timeRange(LocalTime.of(8, 0), LocalTime.of(20, 0))
            .randomizationDepth(3)
            .overrideDefaultInitialization(true);

    private static final EasyRandom ER = new EasyRandom(P);


    /** Один объект любого класса. Каждый вызов — новые значения полей. */
    public static <T> T a(Class<T> type) {
        return ER.nextObject(type);
    }

    /** Несколько объектов. Удобно для параметризованных тестов/подготовки данных. */
    public static <T> List<T> many(Class<T> type, int size) {
        return Stream.generate(() -> a(type)).limit(size).toList();
    }

    private static EasyRandom ER() { return TL.get(); }

    private static final ThreadLocal<EasyRandom> TL = ThreadLocal.withInitial(() -> new EasyRandom(P));

    /** Потокобезопасный
     * Один объект любого класса. Каждый вызов — новые значения полей. */
    public static <T> T aTS(Class<T> type) {
        return ER().nextObject(type);
    }

    /** Потокобезопасный
     * Несколько объектов. Удобно для параметризованных тестов/подготовки данных. */
    public static <T> List<T> manyTS(Class<T> type, int size) {
        return Stream.generate(() -> aTS(type)).limit(size).toList();
    }
}
