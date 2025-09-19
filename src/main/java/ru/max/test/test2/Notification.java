package ru.max.test.test2;

import lombok.AllArgsConstructor;

import java.time.Instant;

@AllArgsConstructor
public class Notification {
   private final String text;
   private final Instant create;
}
