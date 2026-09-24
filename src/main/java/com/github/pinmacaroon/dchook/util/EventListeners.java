package com.github.pinmacaroon.dchook.util;

import com.github.pinmacaroon.dchook.Hook;
import com.github.pinmacaroon.dchook.conf.ModConfigs;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.network.chat.Component;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class EventListeners {

    public static void registerEventListeners(){

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            Hook.setMinecraftServer(server);
            if (!ModConfigs.MESSAGES_SERVER_STARTING_ALLOWED) return;

            HashMap<String, String> request_body = new HashMap<>();
            request_body.put("content", "**"+ModConfigs.MESSAGES_SERVER_STARTING+"**");
            request_body.put("username", "server");

            HttpRequest post = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(Hook.GSON.toJson(request_body)))
                    .uri(Hook.WEBHOOK_URI)
                    .header("Content-Type", "application/json")
                    .build();

            try {
                Hook.HTTPCLIENT.sendAsync(post, HttpResponse.BodyHandlers.ofString()).get().body();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });

        if (ModConfigs.MESSAGES_SERVER_STARTED_ALLOWED && ModConfigs.FUNCTIONS_PROMOTIONS_ENABLED)
            ServerLifecycleEvents.SERVER_STARTED.register(server -> {
                if (ModConfigs.MESSAGES_SERVER_STARTED_ALLOWED) {
                    HashMap<String, String> request_body = new HashMap<>();
                    request_body.put("content", "**"+ModConfigs.MESSAGES_SERVER_STARTED+"**");
                    request_body.put("username", "server");

                    HttpRequest post = HttpRequest.newBuilder()
                            .POST(HttpRequest.BodyPublishers.ofString(Hook.GSON.toJson(request_body)))
                            .uri(Hook.WEBHOOK_URI)
                            .header("Content-Type", "application/json")
                            .build();

                    try {
                        Hook.HTTPCLIENT.sendAsync(post, HttpResponse.BodyHandlers.ofString()).get().body();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                }

                if (ModConfigs.FUNCTIONS_PROMOTIONS_ENABLED) {
                    PromotionProvider.sendPromotion(Hook.WEBHOOK_URI);
                }
            });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            if (ModConfigs.MESSAGES_SERVER_STOPPED_ALLOWED) {
                HashMap<String, String> request_body = new HashMap<>();
                request_body.put("content", "**" + ModConfigs.MESSAGES_SERVER_STOPPED + "**");
                request_body.put("username", "server");

                HttpRequest post = HttpRequest.newBuilder()
                        .POST(HttpRequest.BodyPublishers.ofString(Hook.GSON.toJson(request_body)))
                        .uri(Hook.WEBHOOK_URI)
                        .header("Content-Type", "application/json")
                        .build();

                try {
                    Hook.HTTPCLIENT.sendAsync(post, HttpResponse.BodyHandlers.ofString()).get().body();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }

            if(Hook.BOT != null) Hook.BOT.stop();
        });

        if (ModConfigs.MESSAGES_SERVER_STOPPING_ALLOWED) ServerLifecycleEvents.SERVER_STOPPING.register(server -> {

            HashMap<String, String> request_body = new HashMap<>();
            request_body.put("content", "**"+ModConfigs.MESSAGES_SERVER_STOPPING+"**");
            request_body.put("username", "server");

            HttpRequest post = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(Hook.GSON.toJson(request_body)))
                    .uri(Hook.WEBHOOK_URI)
                    .header("Content-Type", "application/json")
                    .build();

            try {
                Hook.HTTPCLIENT.sendAsync(post, HttpResponse.BodyHandlers.ofString()).get().body();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });

        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, parameters) -> {
            if(message.signedContent().strip().endsWith("//") && ModConfigs.FUNCTIONS_ALLOWOOCMESSAGES) return;

            HashMap<String, String> request_body = new HashMap<>();

            if(XaeoWaypoint.parse(message.signedContent())!=null){
                XaeoWaypoint point = XaeoWaypoint.parse(message.signedContent());
                request_body.put("content", MessageFormat.format(
                        "*"+ModConfigs.MESSAGES_SERVER_WAYPOINT+"*",
                        point.name,
                        point.marker,
                        point.x, point.y, point.z,
                        point.getDimension()
                ));
            } else request_body.put("content", MarkdownSanitizer.escape(message.signedContent()));

            request_body.put("username", sender.getName().getString());
            request_body.put("avatar_url", "https://crafthead.net/helm/" + message.sender().toString());

            HttpRequest post = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(Hook.GSON.toJson(request_body)))
                    .uri(Hook.WEBHOOK_URI)
                    .header("Content-Type", "application/json")
                    .build();

            try {
                Hook.HTTPCLIENT.sendAsync(post, HttpResponse.BodyHandlers.ofString()).get().body();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });

        ServerMessageEvents.GAME_MESSAGE.register((server, text, b) -> {
            if(Component.translatable(text.getString()).getString().startsWith("<")) return;

            HashMap<String, String> request_body = new HashMap<>();
            request_body.put("content", "**"+Component.translatable(text.getString()).getString()+"**");
            request_body.put("username", "game");

            HttpRequest post = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(Hook.GSON.toJson(request_body)))
                    .uri(Hook.WEBHOOK_URI)
                    .header("Content-Type", "application/json")
                    .build();

            try {
                Hook.HTTPCLIENT.sendAsync(post, HttpResponse.BodyHandlers.ofString()).get().body();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
