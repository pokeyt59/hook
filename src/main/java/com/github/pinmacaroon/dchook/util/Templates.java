package com.github.pinmacaroon.dchook.util;

import com.github.pinmacaroon.dchook.Hook;

import java.text.MessageFormat;

/**
 * Fills the {0}, {1}, ... placeholders of the configurable messages.
 * <p>
 * Plain {@link MessageFormat} surprises people writing a config file: an apostrophe starts a quoted section
 * ("It's {0}" turns into "Its {0}") and numbers get grouped (1500 turns into "1,500", bad for coordinates).
 */
public class Templates {
    public static String format(String template, Object... args) {
        Object[] plain = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            plain[i] = args[i] instanceof Number number ? String.valueOf(number) : args[i];
        }
        try {
            return MessageFormat.format(template.replace("'", "''"), plain);
        } catch (IllegalArgumentException e) {
            Hook.LOGGER.warn("couldn't fill in the message '{}', check its {{0}} style placeholders: {}",
                    template, e.getMessage());
            return template;
        }
    }
}
