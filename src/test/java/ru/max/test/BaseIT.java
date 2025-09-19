package ru.max.test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        classes = TestApplication.class, // минимальный конфиг
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@ActiveProfiles("test")
abstract class BaseIT {}
