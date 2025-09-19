package ru.max.test.test1;

import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
class TestRandomConfig {
    @Bean
    EasyRandom easyRandom() { return new EasyRandom(new EasyRandomParameters().seed(123L)); }
}
