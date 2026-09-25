package com.github.pinmacaroon.dchook.util;

import com.github.pinmacaroon.dchook.Hook;
import com.github.pinmacaroon.dchook.bot.Bot;
import com.github.pinmacaroon.dchook.conf.ModConfigs;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Sends messages to the discord webhook in order, without blocking the calling (usually the server) thread.
 * <p>
 * One background thread works through a queue. Messages that pile up while a request is in flight are merged
 * into as few discord messages as possible, and rate limits (429) are waited out instead of losing messages.
 */
public class Webhook {
    private static final int MAX_CONTENT = 2000;
    private static final int MAX_QUEUED = 1000;
    private static final int MAX_ATTEMPTS = 3;

    private record Pending(JsonObject body, CompletableFuture<Void> done) {}

    private static final BlockingQueue<Pending> QUEUE = new LinkedBlockingQueue<>(MAX_QUEUED);
    // completed once the startup check knows whether the webhook exists
    private static final CompletableFuture<Boolean> READY = new CompletableFuture<>();

    static {
        Thread worker = new Thread(Webhook::work, "dchook-webhook");
        worker.setDaemon(true);
        worker.start();
    }

    /**
     * Queues the body for the webhook. Mentions are always disabled, so relayed text can never ping
     * @everyone, @here, roles or users. Failures are logged, never thrown.
     *
     * @param body webhook execute payload, see discord's "execute webhook" docs
     * @return completes when discord accepted the message or it was given up on
     */
    public static CompletableFuture<Void> send(JsonObject body) {
        Pending pending = new Pending(body, new CompletableFuture<>());
        if (!QUEUE.offer(pending)) {
            Hook.LOGGER.warn("more than {} discord messages are waiting, dropping one", MAX_QUEUED);
            pending.done().complete(null);
        }
        return pending.done();
    }

    /**
     * Lets queued messages go out once the webhook is confirmed. With {@code false} every message is dropped,
     * since discord would reject them anyway.
     */
    public static void markReady(boolean usable) {
        READY.complete(usable);
    }

    /**
     * @param avatarUrl may be null for the webhook's default avatar
     */
    public static CompletableFuture<Void> sendText(String username, String content, String avatarUrl) {
        return send(textPayload(username, content, avatarUrl));
    }

    /**
     * Like {@link #sendText}, but for player chat: @name of a discord member becomes a real ping of them
     * (never @everyone, @here or roles), when the bot is running and functions.bot.pings is on.
     */
    public static CompletableFuture<Void> sendChat(String username, String content, String avatarUrl) {
        JsonObject body = textPayload(username, content, avatarUrl);
        body.addProperty(RESOLVE_MENTIONS, true);
        return send(body);
    }

    // internal markers on queued bodies, removed before posting
    private static final String RESOLVE_MENTIONS = "_dchook_resolve_mentions";
    private static final String PINGED = "_dchook_pinged";

    private static void resolveMentions(JsonObject body) {
        if (body.remove(RESOLVE_MENTIONS) == null) return;
        Bot bot = Hook.BOT;
        if (bot == null || !ModConfigs.FUNCTIONS_BOT_PINGS) return;
        Set<Long> pinged = new LinkedHashSet<>();
        body.addProperty("content", Mentions.resolve(body.get("content").getAsString(), bot::findMemberId, pinged));
        if (pinged.isEmpty()) return;
        JsonArray ids = new JsonArray();
        pinged.forEach(id -> ids.add(String.valueOf(id)));
        body.add(PINGED, ids);
    }

    private static void work() {
        try {
            if (!READY.get()) {
                Hook.LOGGER.error("the webhook isn't usable, not sending messages to discord");
                while (true) QUEUE.take().done().complete(null);
            }
        } catch (Exception e) {
            return;
        }
        while (true) {
            List<Pending> batch = new ArrayList<>();
            try {
                batch.add(QUEUE.take());
            } catch (InterruptedException e) {
                return;
            }
            QUEUE.drainTo(batch);
            // before merging, mentions change the length
            for (Pending pending : batch) {
                try {
                    resolveMentions(pending.body());
                } catch (Exception e) {
                    // never let a lookup problem stop the worker, the message just goes out without pings
                    Hook.LOGGER.warn("couldn't resolve @pings in a message: {}", e.toString());
                }
            }

            for (List<Pending> group : merge(batch)) {
                JsonObject body = group.getFirst().body();
                JsonArray pinged = new JsonArray();
                for (Pending pending : group) {
                    JsonElement ids = pending.body().remove(PINGED);
                    if (ids != null) ids.getAsJsonArray().forEach(id -> { if (!pinged.contains(id)) pinged.add(id); });
                }
                for (Pending other : group.subList(1, group.size())) {
                    body.addProperty("content", body.get("content").getAsString() + "\n"
                            + other.body().get("content").getAsString());
                }
                post(withoutMentions(body, pinged));
                group.forEach(pending -> pending.done().complete(null));
            }
        }
    }

    /**
     * Groups consecutive plain text messages from the same sender, as long as they still fit one discord message.
     * Order is kept, embeds and everything else stay on their own.
     */
    static <T> List<List<T>> merge(List<T> items, Function<T, JsonObject> body) {
        List<List<T>> groups = new ArrayList<>();
        List<T> current = null;
        int length = 0;
        for (T item : items) {
            JsonObject json = body.apply(item);
            boolean joins = current != null && isPlainText(json)
                    && sameSender(body.apply(current.getFirst()), json)
                    && length + 1 + json.get("content").getAsString().length() <= MAX_CONTENT;
            if (joins) {
                current.add(item);
                length += 1 + json.get("content").getAsString().length();
            } else {
                current = new ArrayList<>(List.of(item));
                groups.add(current);
                length = isPlainText(json) ? json.get("content").getAsString().length() : MAX_CONTENT;
            }
        }
        return groups;
    }

    private static List<List<Pending>> merge(List<Pending> batch) {
        return merge(batch, Pending::body);
    }

    private static boolean isPlainText(JsonObject body) {
        return body.has("content") && !body.has("embeds");
    }

    private static boolean sameSender(JsonObject a, JsonObject b) {
        return isPlainText(a)
                && Objects.equals(a.get("username"), b.get("username"))
                && Objects.equals(a.get("avatar_url"), b.get("avatar_url"));
    }

    private static void post(JsonObject body) {
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .uri(Hook.WEBHOOK_URI)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(15))
                .build();
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                HttpResponse<String> response = Hook.HTTPCLIENT.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 429) {
                    sleepSeconds(retryAfter(response));
                    continue;
                }
                if (response.statusCode() / 100 != 2) {
                    Hook.LOGGER.warn("discord rejected a webhook message ({}): {}", response.statusCode(), response.body());
                }
                // discord says how many requests are left, wait instead of running into a 429
                if ("0".equals(response.headers().firstValue("X-RateLimit-Remaining").orElse(null))) {
                    sleepSeconds(response.headers().firstValue("X-RateLimit-Reset-After")
                            .map(Double::parseDouble).orElse(1.0));
                }
                return;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                Hook.LOGGER.warn("couldn't send a webhook message: {}", e.toString());
                return;
            }
        }
        Hook.LOGGER.warn("discord kept rate limiting a webhook message, gave up after {} attempts", MAX_ATTEMPTS);
    }

    private static double retryAfter(HttpResponse<String> response) {
        try {
            return JsonParser.parseString(response.body()).getAsJsonObject().get("retry_after").getAsDouble();
        } catch (Exception e) {
            return response.headers().firstValue("Retry-After").map(Double::parseDouble).orElse(1.0);
        }
    }

    private static void sleepSeconds(double seconds) throws InterruptedException {
        Thread.sleep((long) (Math.min(seconds, 30) * 1000));
    }

    static JsonObject textPayload(String username, String content, String avatarUrl) {
        JsonObject body = new JsonObject();
        body.addProperty("content", defuse(content));
        body.addProperty("username", username);
        if (avatarUrl != null) body.addProperty("avatar_url", avatarUrl);
        return body;
    }

    // @everyone, @here and <@id> / <@&id> / <#id> mention syntax, only when not glued to a word (me@here.com stays)
    private static final Pattern MENTION = Pattern.compile("(?<![\\w@])@(?=everyone\\b|here\\b)|<(?=[@#])");

    /**
     * Puts a zero width space into anything discord would turn into a mention. allowed_mentions already stops the
     * notification, but discord still draws @everyone like a ping, so the text itself can't be a mention.
     */
    static String defuse(String content) {
        return MENTION.matcher(content).replaceAll(match -> Matcher.quoteReplacement(match.group() + "\u200B"));
    }

    static JsonObject withoutMentions(JsonObject body) {
        return withoutMentions(body, new JsonArray());
    }

    /**
     * @param users the only users that may be pinged (discord allows up to 100)
     */
    static JsonObject withoutMentions(JsonObject body, JsonArray users) {
        JsonObject allowedMentions = new JsonObject();
        allowedMentions.add("parse", new JsonArray());
        if (!users.isEmpty()) {
            JsonArray allowed = new JsonArray();
            for (int i = 0; i < Math.min(users.size(), 100); i++) allowed.add(users.get(i));
            allowedMentions.add("users", allowed);
        }
        body.add("allowed_mentions", allowedMentions);
        return body;
    }
}
