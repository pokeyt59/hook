package com.github.pinmacaroon.dchook.bot.commands;

import com.github.pinmacaroon.dchook.conf.ModConfigs;
import com.github.pinmacaroon.dchook.util.Templates;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.RestAction;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModEnvironment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ModsCommand {
    public static void run(SlashCommandInteractionEvent event) {
        StringBuilder mod_list = new StringBuilder();
        AtomicInteger mods_count = new AtomicInteger();
        FabricLoader.getInstance().getAllMods().forEach(modContainer -> {
            if (modContainer.getMetadata().getEnvironment() == ModEnvironment.UNIVERSAL
                    && !modContainer.getMetadata().getType().equals("builtin")
                    && !modContainer.getMetadata().getId().startsWith("fabric")
                    && !modContainer.getMetadata().getId().equals("mixinextras")
                    && modContainer.getContainingMod().isEmpty()) {
                mod_list.append(modContainer.getMetadata().getName()).append(' ')
                        .append(modContainer.getMetadata().getVersion().getFriendlyString()).append('\n');
                mods_count.getAndIncrement();
            }
        });
        String response;
        if (mods_count.get() == 0) {
            response = ModConfigs.MESSAGES_BOT_MODS_NONE;
        } else {
            response = Templates.format(ModConfigs.MESSAGES_BOT_MODS_LIST, mods_count.get()) + "\n" + mod_list;
        }
        boolean ephemeral = event.getOption("ephemeral", false, OptionMapping::getAsBoolean);
        if (event.getOption("full", false, OptionMapping::getAsBoolean) && response.length() > 2000) {
            List<String> messages = splitMessages(response);
            // chain the follow-ups so they arrive in order after the first reply
            RestAction<?> action = event.reply(messages.getFirst()).setEphemeral(ephemeral);
            for (String message : messages.subList(1, messages.size())) {
                action = action.flatMap(ignored -> event.getHook().sendMessage(message).setEphemeral(ephemeral));
            }
            action.queue();
            return;
        }
        if (response.length() > 2000) {
            // cut at the last whole line that fits, discord messages are capped at 2000 characters
            String[] lines = response.split("\n");
            StringBuilder shortened = new StringBuilder(lines[0]);
            int shown = 1;
            while (shown < lines.length
                    && shortened.length() + 1 + lines[shown].length() + "\n...and 999 more".length() <= 2000) {
                shortened.append('\n').append(lines[shown++]);
            }
            response = shortened.append("\n...and ").append(lines.length - shown).append(" more").toString();
        }

        event.reply(response).setEphemeral(ephemeral).queue();
    }

    // splits at line breaks into messages of at most 2000 characters, discord's message limit
    private static List<String> splitMessages(String text) {
        List<String> messages = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String line : text.split("\n")) {
            if (!current.isEmpty() && current.length() + 1 + line.length() > 2000) {
                messages.add(current.toString());
                current.setLength(0);
            }
            if (!current.isEmpty()) current.append('\n');
            current.append(line.length() > 2000 ? line.substring(0, 2000) : line);
        }
        if (!current.isEmpty()) messages.add(current.toString());
        return messages;
    }
}
