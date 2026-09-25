package com.github.pinmacaroon.dchook.conf;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimpleConfigAppendixTest {
    private static final Map<String, String> ENTRIES = new LinkedHashMap<>();

    static {
        ENTRIES.put("a", "# first\na=1\n");
        ENTRIES.put("b", "# second\nb=2\n");
        ENTRIES.put("c", "# third\nc=3\n");
    }

    @Test
    void nothingMissing() {
        assertEquals("", SimpleConfig.appendix(Map.of("a", "x", "b", "y", "c", "z"), ENTRIES, "added"));
    }

    @Test
    void appendsOnlyMissingInOrder() {
        assertEquals("\n# added\n# first\na=1\n# third\nc=3\n",
                SimpleConfig.appendix(Map.of("b", "changed"), ENTRIES, "added"));
    }

    @Test
    void emptyFileGetsEverything() {
        assertEquals("\n# added\n# first\na=1\n# second\nb=2\n# third\nc=3\n",
                SimpleConfig.appendix(Map.of(), ENTRIES, "added"));
    }
}
