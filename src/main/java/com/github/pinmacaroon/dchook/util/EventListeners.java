package com.github.pinmacaroon.dchook.util;

import com.github.pinmacaroon.dchook.Hook;
import com.github.pinmacaroon.dchook.conf.ModConfigs;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.network.chat.Component;

import java.text.MessageFormat;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class EventListeners {

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
            if(Hook.BOT != null) Hook.BOT.stop();
        });

        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, parameters) -> {
            if(message.signedContent().strip().endsWith("//") && ModConfigs.FUNCTIONS_ALLOWOOCMESSAGES) return;

            String content;
            XaeoWaypoint point = XaeoWaypoint.parse(message.signedContent());
            if(point != null){
                content = MessageFormat.format(
                        "*"+ModConfigs.MESSAGES_SERVER_WAYPOINT+"*",
                        point.name,
                        point.marker,
                        point.x, point.y, point.z,
                        point.getDimension()
                );
            } else content = MarkdownSanitizer.escape(message.signedContent());

            Webhook.sendText(sender.getName().getString(), content,
                    "https://crafthead.net/helm/" + message.sender().toString());
        });

        ServerMessageEvents.GAME_MESSAGE.register((server, text, b) -> {
            if(Component.translatable(text.getString()).getString().startsWith("<")) return;

            Webhook.sendText("game", "**"+Component.translatable(text.getString()).getString()+"**", null);
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
