package com.github.pinmacaroon.dchook.util;

import com.github.pinmacaroon.dchook.Hook;
import com.github.pinmacaroon.dchook.conf.ModConfigs;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class EventListeners {
    /**
     * true while a discord message is being broadcast in game (server thread only), so GAME_MESSAGE
     * doesn't send it straight back to discord
     */
    public static boolean RELAYING = false;

    public static void registerEventListeners(){

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            Hook.setMinecraftServer(server);
            if (ModConfigs.MESSAGES_SERVER_STARTING_ALLOWED)
                Webhook.sendText("server", "**" + ModConfigs.MESSAGES_SERVER_STARTING + "**", null);
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if (ModConfigs.MESSAGES_SERVER_STARTED_ALLOWED)
                Webhook.sendText("server", "**" + ModConfigs.MESSAGES_SERVER_STARTED + "**", null);
            if (ModConfigs.FUNCTIONS_PROMOTIONS_ENABLED) PromotionProvider.sendPromotion();
        });

        // the process may exit right after these, so wait (briefly) until discord got them
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if (ModConfigs.MESSAGES_SERVER_STOPPING_ALLOWED)
                awaitBriefly(Webhook.sendText("server", "**" + ModConfigs.MESSAGES_SERVER_STOPPING + "**", null));
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            if (ModConfigs.MESSAGES_SERVER_STOPPED_ALLOWED)
                awaitBriefly(Webhook.sendText("server", "**" + ModConfigs.MESSAGES_SERVER_STOPPED + "**", null));
            Hook.SERVER_STOPPED = true;
            if(Hook.BOT != null) Hook.BOT.stop();
        });

        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, parameters) -> {
            if(message.signedContent().strip().endsWith("//") && ModConfigs.FUNCTIONS_ALLOWOOCMESSAGES) return;

            String content;
            XaeoWaypoint point = XaeoWaypoint.parse(message.signedContent());
            if(point != null){
                content = Templates.format(
                        "*"+ModConfigs.MESSAGES_SERVER_WAYPOINT+"*",
                        point.name,
                        point.marker,
                        point.x, point.y, point.z,
                        point.getDimension()
                );
            } else {
                String text = message.signedContent();
                if (ModConfigs.FUNCTIONS_EMOJI_SHORTCODES) text = Emojis.fromShortcodes(text);
                content = MarkdownSanitizer.escape(text);
            }

            Webhook.sendChat(sender.getName().getString(), content,
                    "https://crafthead.net/helm/" + message.sender().toString());
        });

        // GAME_MESSAGE is every system message sent to all players: joins, leaves, deaths, advancements,
        // /say and friends, but also our own discord relays and action bar ("overlay") text
        ServerMessageEvents.GAME_MESSAGE.register((server, text, overlay) -> {
            if (overlay || RELAYING) return;
            Webhook.sendText("game", "**" + MarkdownSanitizer.escape(text.getString()) + "**", null);
        });
    }

    private static void awaitBriefly(CompletableFuture<Void> send) {
        try {
            send.orTimeout(5, TimeUnit.SECONDS).join();
        } catch (Exception e) {
            Hook.LOGGER.warn("discord didn't confirm the message in time: {}", e.toString());
        }
    }
}
