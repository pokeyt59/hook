package com.github.pinmacaroon.dchook.util;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DiscordTimestampsTest {
    private static final ZoneId UTC = ZoneId.of("UTC");
    private static final Instant NOW = Instant.ofEpochSecond(1759000000L);

    private static String render(String text) {
        return DiscordTimestamps.render(text, UTC, NOW);
    }

    @Test
    void absoluteStyles() {
        assertEquals("7:06 PM UTC", render("<t:1759000000:t>"));
        assertEquals("7:06:40 PM UTC", render("<t:1759000000:T>"));
        assertEquals("09/27/2025", render("<t:1759000000:d>"));
        assertEquals("September 27, 2025", render("<t:1759000000:D>"));
        assertEquals("September 27, 2025 7:06 PM UTC", render("<t:1759000000:f>"));
        assertEquals("Saturday, September 27, 2025 7:06 PM UTC", render("<t:1759000000:F>"));
        assertEquals("September 27, 2025 7:06 PM UTC", render("<t:1759000000>"));
    }

    @Test
    void relativeStyle() {
        assertEquals("a few seconds ago", render("<t:1759000000:R>"));
        assertEquals("in 5 minutes", render("<t:1759000300:R>"));
        assertEquals("2 days ago", render("<t:1758827200:R>"));
        assertEquals("in 1 year", render("<t:1790536000:R>"));
    }

    @Test
    void leavesOtherTextAlone() {
        assertEquals("no timestamps here, <t:abc> $1 \\", render("no timestamps here, <t:abc> $1 \\"));
        assertEquals("from 7:06 PM UTC to 7:11 PM UTC", render("from <t:1759000000:t> to <t:1759000300:t>"));
    }
}
