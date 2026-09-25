package com.github.pinmacaroon.dchook.util;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WebhookPayloadTest {
    @Test
    void mentionsAreAlwaysDisabled() {
        JsonObject body = Webhook.withoutMentions(Webhook.textPayload("Steve", "@everyone hi", null));
        assertEquals(0, body.getAsJsonObject("allowed_mentions").getAsJsonArray("parse").size());
        assertEquals("@\u200Beveryone hi", body.get("content").getAsString());
        assertFalse(body.has("avatar_url"));
    }

    @Test
    void avatarIsSetWhenGiven() {
        JsonObject body = Webhook.textPayload("Steve", "hi", "https://example.com/a.png");
        assertEquals("https://example.com/a.png", body.get("avatar_url").getAsString());
    }

    @Test
    void mentionsAreDefused() {
        assertEquals("@\u200Beveryone and @\u200Bhere!", Webhook.defuse("@everyone and @here!"));
        assertEquals("<\u200B@123> <\u200B@!123> <\u200B@&456> <\u200B#789>", Webhook.defuse("<@123> <@!123> <@&456> <#789>"));
    }

    @Test
    void normalTextIsUntouched() {
        for (String text : new String[]{"me@here.com", "@Steve hi", "@everyones", "a < b", "<3", "hello everyone"}) {
            assertEquals(text, Webhook.defuse(text));
        }
    }
}
