package com.github.pinmacaroon.dchook.util;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WebhookPayloadTest {
    @Test
    void mentionsAreAlwaysDisabled() {
        JsonObject body = Webhook.withoutMentions(Webhook.textPayload("Steve", "@everyone hi", null));
        assertEquals(0, body.getAsJsonObject("allowed_mentions").getAsJsonArray("parse").size());
        assertEquals("@everyone hi", body.get("content").getAsString());
        assertFalse(body.has("avatar_url"));
    }

    @Test
    void avatarIsSetWhenGiven() {
        JsonObject body = Webhook.textPayload("Steve", "hi", "https://example.com/a.png");
        assertEquals("https://example.com/a.png", body.get("avatar_url").getAsString());
    }
}
