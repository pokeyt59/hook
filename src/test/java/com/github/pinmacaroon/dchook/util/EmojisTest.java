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
    void emojiLinksFromOtherServersBecomeShortcodes() {
        assertEquals("hi :2kissLumity2:", Emojis.toShortcodes("hi [2kissLumity2](https://cdn.discordapp.com/emojis/"
                + "1192568648997543956.webp?size=48&animated=true&name=2kissLumity2&lossless=true)"));
        assertEquals(":blob:", Emojis.toShortcodes("[:blob:](<https://cdn.discordapp.com/emojis/123.png>)"));
        assertEquals("gg :2kissLumity2: :emoji:", Emojis.toShortcodes("gg https://cdn.discordapp.com/emojis/1192568648997543956"
                + ".webp?size=48&animated=true&name=2kissLumity2 https://cdn.discordapp.com/emojis/1.png"));
        String link = "[docs](https://cdn.discordapp.com/attachments/1/2/file.png)";
        assertEquals(link, Emojis.toShortcodes(link));
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
