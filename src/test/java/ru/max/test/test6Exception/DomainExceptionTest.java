package ru.max.test.test6Exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import ru.max.test.test6.DomainException.NotFound;


class DomainExceptionTest {

    @Test
    void error() {
        NotFound notFound = new NotFound("Order", "5");

        assertEquals("NOT_FOUND", notFound.code());
        assertEquals(404, notFound.status());
        assertEquals("Order 5 not found", notFound.getMessage());
        assertTrue(notFound.props().containsValue("5"));
        assertNull(notFound.getCause());
    }

}