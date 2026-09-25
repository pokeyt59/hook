package com.github.pinmacaroon.dchook.util;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MentionsTest {
    private static final Map<String, Long> MEMBERS = Map.of("pokey", 11L, "tim_bot", 22L, "a.b", 33L);

    private static String resolve(String text, Set<Long> pinged) {
        return Mentions.resolve(text, name -> MEMBERS.get(name.toLowerCase()), pinged);
    }

    @Test
    void knownNamesBecomeMentions() {
        Set<Long> pinged = new LinkedHashSet<>();
        assertEquals("hi <@11> and <@22>", resolve("hi @Pokey and @tim\\_bot", pinged));
        assertEquals(Set.of(11L, 22L), pinged);
    }

    @Test
    void sentenceDotsStayOutside() {
        Set<Long> pinged = new LinkedHashSet<>();
        assertEquals("look <@11>.", resolve("look @pokey.", pinged));
        assertEquals("<@33>!", resolve("@a.b!", pinged));
    }

    @Test
    void unknownAndNonMentionsStay() {
        Set<Long> pinged = new LinkedHashSet<>();
        for (String text : new String[]{"@nobody here", "me@pokey.com", "@​everyone", "@", "@@pokey", "a @x b"}) {
            assertEquals(text, resolve(text, pinged));
        }
        assertEquals(Set.of(), pinged);
    }
}
