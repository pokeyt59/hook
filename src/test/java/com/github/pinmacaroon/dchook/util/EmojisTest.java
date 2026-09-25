package com.github.pinmacaroon.dchook.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmojisTest {
    @Test
    void emojisBecomeDiscordShortcodes() {
        assertEquals("hi :grinning: :thumbsup_tone3: :heart: :flag_us:", Emojis.toShortcodes("hi 😀 👍🏽 ❤️ 🇺🇸"));
        assertEquals("no emojis here", Emojis.toShortcodes("no emojis here"));
    }

    @Test
    void shortcodesBecomeEmojis() {
        assertEquals("hi 😂 🔥", Emojis.fromShortcodes("hi :joy: :fire:"));
        assertEquals("😂!", Emojis.fromShortcodes(":joy:!"));
        assertEquals("❤️🔥", Emojis.fromShortcodes(":heart::fire:"));
    }

    @Test
    void otherColonsStayAsTheyAre() {
        for (String text : new String[]{"at 12:30:45", "a:b:c", ":not_an_emoji:", "ratio: 3:2", "::"}) {
            assertEquals(text, Emojis.fromShortcodes(text));
        }
    }

    @Test
    void roundTrip() {
        String text = "gg 🎉 :tada:";
        assertEquals("gg 🎉 🎉", Emojis.fromShortcodes(text));
        assertEquals("gg :tada: :tada:", Emojis.toShortcodes(Emojis.fromShortcodes(text)));
    }
}
