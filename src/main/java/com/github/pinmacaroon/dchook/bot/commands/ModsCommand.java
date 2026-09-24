package com.github.pinmacaroon.dchook.bot.commands;

import com.github.pinmacaroon.dchook.conf.ModConfigs;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModEnvironment;

import java.text.MessageFormat;
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
            response = MessageFormat.format(ModConfigs.MESSAGES_BOT_MODS_LIST, mods_count.get()) + "\n" + mod_list;
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

        event.reply(response).setEphemeral(event.getOption("ephemeral", false, OptionMapping::getAsBoolean)).queue();
    }
}
