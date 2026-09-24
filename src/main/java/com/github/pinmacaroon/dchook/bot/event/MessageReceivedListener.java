package com.github.pinmacaroon.dchook.bot.event;

import com.github.pinmacaroon.dchook.Hook;
import com.github.pinmacaroon.dchook.bot.Bot;
import com.github.pinmacaroon.dchook.conf.ModConfigs;
import com.github.pinmacaroon.dchook.util.DiscordTimestamps;
import com.github.pinmacaroon.dchook.util.EventListeners;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReference;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public class MessageReceivedListener extends ListenerAdapter {
    private final Bot BOT;

    public MessageReceivedListener(Bot bot) {
        this.BOT = bot;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getGuild().getIdLong() != this.BOT.getGUILD_ID()) return;
        if (event.getMessage().getAuthor().isBot()) return;
        MinecraftServer server = Hook.getGameServer();
        if (event.getChannel().getIdLong() == this.BOT.getCHANNEL_ID() && server != null) {
            if (event.getMessage().getContentStripped().endsWith("//") && ModConfigs.FUNCTIONS_ALLOWOOCMESSAGES) return;
            MutableComponent message = renderMessage(event.getMessage());
            // JDA events arrive on JDA's threads, the player list must only be touched from the server thread
            server.execute(() -> {
                // broadcasting fires GAME_MESSAGE, the flag keeps it from being relayed back to discord
                EventListeners.RELAYING = true;
                try {
                    server.getPlayerList().broadcastSystemMessage(message, false);
                } finally {
                    EventListeners.RELAYING = false;
                }
            });
        }
    }

    private static MutableComponent renderMessage(Message message) {
        final String raw_message = DiscordTimestamps.render(message.getContentDisplay(),
                ModConfigs.FUNCTIONS_TIMESTAMP_ZONE, Instant.now());
        MutableComponent signature;
        MutableComponent reply;
        MutableComponent content;

        MessageReference r = message.getMessageReference();
        if (r != null) {
            reply = Component.literal("<@%s -> ".formatted(
                    r.getMessage().getAuthor().getName()
            ));
        } else {
            reply = Component.literal("<");
        }

        signature = Component.literal("@%s> ".formatted(
                message.getAuthor().getName()
        ));

        content = (raw_message.isBlank())
                ? Component.literal("[embed]")
                : Component.literal(raw_message);

        // 0x5555FF is the old Formatting.BLUE, ChatFormatting is being phased out in favour of Style colors
        return reply.append(signature).append(content).withStyle(style -> style.withColor(0x5555FF));
    }
}
