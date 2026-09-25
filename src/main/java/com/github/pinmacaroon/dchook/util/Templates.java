package com.github.pinmacaroon.dchook.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

/**
 * Fills the {0}, {1}, ... placeholders of the configurable messages.
 * <p>
 * Plain {@link MessageFormat} surprises people writing a config file: an apostrophe starts a quoted section
 * ("It's {0}" turns into "Its {0}") and numbers get grouped (1500 turns into "1,500", bad for coordinates).
 */
public class Templates {
    // same logger as Hook.LOGGER, without loading Hook (it needs a running fabric loader, unit tests have none)
    private static final Logger LOGGER = LoggerFactory.getLogger("dchook");

    public static String format(String template, Object... args) {
        Object[] plain = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            plain[i] = args[i] instanceof Number number ? String.valueOf(number) : args[i];
        }
        try {
            return MessageFormat.format(template.replace("'", "''"), plain);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("couldn't fill in the message '{}', check its {0} style placeholders: {}",
                    template, e.getMessage());
            return template;
        }
    }
}
