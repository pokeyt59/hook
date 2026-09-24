package com.github.pinmacaroon.dchook.util;

import com.github.pinmacaroon.dchook.Hook;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Sends messages to the discord webhook without blocking the calling (usually the server) thread.
 */
public class Webhook {

    /**
     * Posts the body to the webhook. Mentions are always disabled, so relayed text can never ping
     * @everyone, @here, roles or users. Failures are logged, never thrown.
     *
     * @param body webhook execute payload, see discord's "execute webhook" docs
     * @return completes when discord answered or the request failed
     */
    public static CompletableFuture<Void> send(JsonObject body) {
        HttpRequest post = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(withoutMentions(body).toString()))
                .uri(Hook.WEBHOOK_URI)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(15))
                .build();
        return Hook.HTTPCLIENT.sendAsync(post, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() / 100 != 2) {
                        Hook.LOGGER.warn("discord rejected a webhook message ({}): {}",
                                response.statusCode(), response.body());
                    }
                })
                .exceptionally(e -> {
                    Hook.LOGGER.warn("couldn't send a webhook message: {}", e.toString());
                    return null;
                });
    }

    /**
     * @param avatarUrl may be null for the webhook's default avatar
     */
    public static CompletableFuture<Void> sendText(String username, String content, String avatarUrl) {
        return send(textPayload(username, content, avatarUrl));
    }

    static JsonObject textPayload(String username, String content, String avatarUrl) {
        JsonObject body = new JsonObject();
        body.addProperty("content", content);
        body.addProperty("username", username);
        if (avatarUrl != null) body.addProperty("avatar_url", avatarUrl);
        return body;
    }

    static JsonObject withoutMentions(JsonObject body) {
        JsonObject allowedMentions = new JsonObject();
        allowedMentions.add("parse", new JsonArray());
        body.add("allowed_mentions", allowedMentions);
        return body;
    }
}
