package ru.max.test.test1;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class Message {
    @EqualsAndHashCode.Include
    private final String id;
    private final long createdAt;
    private final String text;

}

