package com.github.pinmacaroon.dchook.bot.event;

import com.github.pinmacaroon.dchook.Hook;
import com.github.pinmacaroon.dchook.bot.Bot;
import com.github.pinmacaroon.dchook.conf.ModConfigs;
import com.github.pinmacaroon.dchook.util.DiscordTimestamps;
import com.github.pinmacaroon.dchook.util.EventListeners;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.messages.MessageSnapshot;
import net.dv8tion.jda.api.entities.sticker.StickerItem;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MessageReceivedListener extends ListenerAdapter {
    private static final Pattern CUSTOM_EMOJI = Pattern.compile("<a?:(\\w+):\\d+>");

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
            MutableComponent message = renderMessage(event);
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

    private static MutableComponent renderMessage(MessageReceivedEvent event) {
        Message message = event.getMessage();
        // the server nickname like discord shows it, the member is always sent along with guild messages
        String author = event.getMember() != null
                ? event.getMember().getEffectiveName()
                : message.getAuthor().getEffectiveName();

        // null when the replied-to message was deleted, the payload has no member for it so no nickname either
        Message replied = message.getReferencedMessage();
        MutableComponent result = Component.literal(replied != null
                ? "<@%s -> @%s> ".formatted(replied.getAuthor().getEffectiveName(), author)
                : "<@%s> ".formatted(author));

        List<Component> parts = new ArrayList<>();
        addParts(parts, message.getContentDisplay(), message.getAttachments(), message.getStickers());
        // forwarded messages only come with raw content, so custom emojis are converted here instead of by JDA
        for (MessageSnapshot forwarded : message.getMessageSnapshots()) {
            parts.add(Component.literal("[forwarded]"));
            addParts(parts, CUSTOM_EMOJI.matcher(forwarded.getContentRaw()).replaceAll(":$1:"),
                    forwarded.getAttachments(), forwarded.getStickers());
        }
        if (parts.isEmpty()) parts.add(Component.literal("[embed]"));
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) result.append(" ");
            result.append(parts.get(i));
        }

        // 0x5555FF is the old Formatting.BLUE, ChatFormatting is being phased out in favour of Style colors
        return result.withStyle(style -> style.withColor(0x5555FF));
    }

    private static void addParts(List<Component> parts, String text, List<Message.Attachment> attachments,
                                 List<StickerItem> stickers) {
        String content = DiscordTimestamps.render(text, ModConfigs.FUNCTIONS_TIMESTAMP_ZONE, Instant.now());
        if (!content.isBlank()) parts.add(Component.literal(content));
        for (Message.Attachment attachment : attachments) {
            String kind = attachment.isImage() ? "image" : attachment.isVideo() ? "video" : "file";
            parts.add(link("[%s: %s]".formatted(kind, attachment.getFileName()), attachment.getUrl()));
        }
        for (StickerItem sticker : stickers) {
            parts.add(Component.literal("[sticker: %s]".formatted(sticker.getName())));
        }
    }

    private static MutableComponent link(String text, String url) {
        try {
            URI uri = URI.create(url);
            return Component.literal(text).withStyle(style -> style
                    .withUnderlined(true)
                    .withClickEvent(new ClickEvent.OpenUrl(uri))
                    .withHoverEvent(new HoverEvent.ShowText(Component.literal("open in browser"))));
        } catch (IllegalArgumentException e) {
            return Component.literal(text);
        }
    }
}
