package com.github.pinmacaroon.dchook.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WebhookMergeTest {
    private static JsonObject text(String user, String content) {
        return Webhook.textPayload(user, content, "https://crafthead.net/helm/" + user);
    }

    private static List<List<JsonObject>> merge(JsonObject... bodies) {
        return Webhook.merge(List.of(bodies), Function.identity());
    }

    @Test
    void mergesConsecutiveMessagesFromTheSameSender() {
        JsonObject a = text("Steve", "hi"), b = text("Steve", "again");
        assertEquals(List.of(List.of(a, b)), merge(a, b));
    }

    @Test
    void keepsOrderAcrossSenders() {
        JsonObject a = text("Steve", "1"), b = text("Alex", "2"), c = text("Steve", "3");
        assertEquals(List.of(List.of(a), List.of(b), List.of(c)), merge(a, b, c));
    }

    @Test
    void neverMergesEmbeds() {
        JsonObject embed = new JsonObject();
        embed.addProperty("username", "Steve");
        embed.add("embeds", new JsonArray());
        JsonObject a = text("Steve", "hi");
        assertEquals(List.of(List.of(a), List.of(embed)), merge(a, embed));
    }

    @Test
    void staysUnderDiscordsLengthLimit() {
        JsonObject a = text("Steve", "x".repeat(1500)), b = text("Steve", "y".repeat(600));
        assertEquals(List.of(List.of(a), List.of(b)), merge(a, b));
    }
}
