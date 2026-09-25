package com.github.pinmacaroon.dchook.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TemplatesTest {
    @Test
    void fillsPlaceholders() {
        assertEquals("There are 3/20 players online", Templates.format("There are {0}/{1} players online", 3, 20));
    }

    @Test
    void keepsApostrophes() {
        assertEquals("It's 12:00, it's clear", Templates.format("It's {0}, it's {1}", "12:00", "clear"));
    }

    @Test
    void doesNotGroupNumbers() {
        assertEquals("at 1500, -12345, 64", Templates.format("at {0}, {1}, {2}", 1500, -12345, 64));
        assertEquals("day 123456", Templates.format("day {0}", 123456L));
    }

    @Test
    void unusedAndMissingPlaceholders() {
        assertEquals("no placeholders", Templates.format("no placeholders", "unused"));
        assertEquals("only {1}", Templates.format("only {1}", "first"));
    }

    @Test
    void brokenTemplateIsReturnedAsIs() {
        assertEquals("oops {0", Templates.format("oops {0", "x"));
    }
}
