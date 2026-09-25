package com.github.pinmacaroon.dchook.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns @name in relayed minecraft chat into a real discord mention of that member.
 */
public class Mentions {
    // @name, not glued to other text (me@mail.com stays). the chat text is markdown escaped, so _ arrives as \_
    // each unknown name costs a request to discord, a message full of @words shouldn't hold up the chat
    static final int MAX_LOOKUPS = 5;
    private static final Pattern NAME = Pattern.compile("(?<![\\w@\\\\])@((?:[A-Za-z0-9._]|\\\\_){2,40})");

    /**
     * @param lookup  member name -> user id, null when nobody has that name
     * @param pinged  collects the ids that were mentioned, discord only pings users listed in allowed_mentions
     */
    static String resolve(String content, Function<String, Long> lookup, Set<Long> pinged) {
        if (content.indexOf('@') < 0) return content;
        Matcher matcher = NAME.matcher(content);
        StringBuilder result = new StringBuilder();
        Map<String, Long> looked = new HashMap<>();
        while (matcher.find()) {
            String name = matcher.group(1).replace("\\_", "_");
            // "hi @steve." ends the sentence, the dot isn't part of the name
            String trailing = "";
            while (name.endsWith(".")) {
                name = name.substring(0, name.length() - 1);
                trailing += ".";
            }
            String key = name.toLowerCase(Locale.ROOT);
            Long id = null;
            if (name.length() >= 2 && (looked.containsKey(key) || looked.size() < MAX_LOOKUPS)) {
                id = looked.containsKey(key) ? looked.get(key) : lookup.apply(name);
                looked.put(key, id);
            }
            String replacement = id == null ? matcher.group() : "<@" + id + ">" + trailing;
            if (id != null) pinged.add(id);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
