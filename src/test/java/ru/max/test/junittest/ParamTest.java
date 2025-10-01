package ru.max.test.junittest;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ParamTest {
    static int abs(int x) {
        return x < 0 ? -x : x;
    }

    @ParameterizedTest
    @CsvSource({"-1, 1", "0, 0", "5, 5"})
    void csv(int in, int out) {
        assertThat(abs(in)).isEqualTo(out);
    }

    static Stream<Integer> ints() {
        return Stream.of(-10, 2, 7);
    }

    @ParameterizedTest
    @MethodSource("ints")
    void methodSource(int x) {
        assertThat(abs(x)).isGreaterThanOrEqualTo(0);
    }
}
