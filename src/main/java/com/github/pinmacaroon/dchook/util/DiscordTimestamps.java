package com.github.pinmacaroon.dchook.util;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns discord timestamp markup ({@code <t:1759000000:R>}) into readable text for minecraft chat.
 */
public class DiscordTimestamps {
    private static final Pattern TIMESTAMP = Pattern.compile("<t:(-?\\d{1,13})(?::([tTdDfFR]))?>");

    private static final DateTimeFormatter SHORT_TIME = DateTimeFormatter.ofPattern("h:mm a", Locale.US);
    private static final DateTimeFormatter LONG_TIME = DateTimeFormatter.ofPattern("h:mm:ss a", Locale.US);
    private static final DateTimeFormatter SHORT_DATE = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.US);
    private static final DateTimeFormatter LONG_DATE = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.US);
    private static final DateTimeFormatter SHORT_DATE_TIME = DateTimeFormatter.ofPattern("MMMM d, yyyy h:mm a", Locale.US);
    private static final DateTimeFormatter LONG_DATE_TIME = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy h:mm a", Locale.US);

    /**
     * @param zone time zone for absolute times, its short name is appended to times so players in
     *             other zones know what they're reading
     * @param now  the current time, used by relative ({@code :R}) timestamps
     */
    public static String render(String text, ZoneId zone, Instant now) {
        Matcher matcher = TIMESTAMP.matcher(text);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            Instant instant = Instant.ofEpochSecond(Long.parseLong(matcher.group(1)));
            String style = matcher.group(2) == null ? "f" : matcher.group(2);
            matcher.appendReplacement(result, Matcher.quoteReplacement(format(instant, style, zone, now)));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static String format(Instant instant, String style, ZoneId zone, Instant now) {
        ZonedDateTime time = instant.atZone(zone);
        String zoneName = " " + zone.getDisplayName(TextStyle.SHORT, Locale.US);
        return switch (style) {
            case "t" -> SHORT_TIME.format(time) + zoneName;
            case "T" -> LONG_TIME.format(time) + zoneName;
            case "d" -> SHORT_DATE.format(time);
            case "D" -> LONG_DATE.format(time);
            case "F" -> LONG_DATE_TIME.format(time) + zoneName;
            case "R" -> relative(instant, now);
            default -> SHORT_DATE_TIME.format(time) + zoneName;
        };
    }

    private static String relative(Instant instant, Instant now) {
        long seconds = Duration.between(now, instant).getSeconds();
        boolean future = seconds > 0;
        seconds = Math.abs(seconds);
        String amount;
        if (seconds < 45) amount = "a few seconds";
        else if (seconds < 3600) amount = plural(Math.max(1, Math.round(seconds / 60.0)), "minute");
        else if (seconds < 86400) amount = plural(Math.round(seconds / 3600.0), "hour");
        else if (seconds < 86400L * 30) amount = plural(Math.round(seconds / 86400.0), "day");
        else if (seconds < 86400L * 365) amount = plural(Math.round(seconds / (86400.0 * 30)), "month");
        else amount = plural(Math.round(seconds / (86400.0 * 365)), "year");
        return future ? "in " + amount : amount + " ago";
    }

    private static String plural(long count, String unit) {
        return count + " " + unit + (count == 1 ? "" : "s");
    }
}
