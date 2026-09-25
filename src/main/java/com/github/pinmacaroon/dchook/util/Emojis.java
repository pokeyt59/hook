package com.github.pinmacaroon.dchook.util;

import net.fellbaum.jemoji.Emoji;
import net.fellbaum.jemoji.EmojiManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts between unicode emojis and discord's :shortcodes:. Minecraft's font can't draw most emojis, and
 * discord doesn't turn :shortcodes: in webhook messages into emojis, so each side gets what it can show.
 */
public class Emojis {
    // :name: not glued to other text, so times (12:30:45) and things like a:b:c stay as they are
    // an emoji the sender can't use natively (from another server) arrives as a link to its image
    private static final Pattern EMOJI_LINK = Pattern.compile(
            "\\[([^\\]\\n]{1,64})\\]\\(<?https://(?:cdn|media)\\.discordapp\\.(?:com|net)/emojis/\\d+\\.\\w+[^)\\s]*>?\\)");
    private static final Pattern SHORTCODE = Pattern.compile("(?<!\\w):([a-z0-9_+\\-]+):(?!\\w)");

    /**
     * Loads the emoji table (a few hundred milliseconds) so the first message doesn't wait for it.
     */
    public static void preload() {
        EmojiManager.containsAnyEmoji("");
    }

    /**
     * 😀 → :grinning:, using discord's names so they match what people typed there
     */
    public static String toShortcodes(String text) {
        if (text.contains("/emojis/")) {
            text = EMOJI_LINK.matcher(text).replaceAll(match -> Matcher.quoteReplacement(
                    ":" + match.group(1).replace(":", "") + ":"));
        }
        if (!EmojiManager.containsAnyEmoji(text)) return text;
        return EmojiManager.replaceAllEmojis(text, Emojis::shortcode);
    }

    /**
     * :grinning: → 😀 for discord's names, anything else that looks like one stays as it is
     */
    public static String fromShortcodes(String text) {
        if (text.indexOf(':') < 0) return text;
        Matcher matcher = SHORTCODE.matcher(text);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String emoji = EmojiManager.getByDiscordAlias(matcher.group()).map(Emoji::getEmoji).orElse(matcher.group());
            matcher.appendReplacement(result, Matcher.quoteReplacement(emoji));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static String shortcode(Emoji emoji) {
        if (!emoji.getDiscordAliases().isEmpty()) return emoji.getDiscordAliases().getFirst();
        if (!emoji.getAllAliases().isEmpty()) return emoji.getAllAliases().getFirst();
        return emoji.getEmoji();
    }
}
