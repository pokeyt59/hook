package com.github.pinmacaroon.dchook.bot.commands;

import com.github.pinmacaroon.dchook.Hook;
import com.github.pinmacaroon.dchook.conf.ModConfigs;
import com.github.pinmacaroon.dchook.util.Templates;
import com.github.pinmacaroon.dchook.util.TimeConverter;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.minecraft.server.level.ServerLevel;

public class TimeCommand {
    public static void run(SlashCommandInteractionEvent event) {
        ServerLevel overworld = Hook.getGameServer().overworld();
        long time = overworld.getOverworldClockTime();
        String weather = overworld.isThundering() ? ModConfigs.MESSAGES_BOT_TIME_THUNDER
                : overworld.isRaining() ? ModConfigs.MESSAGES_BOT_TIME_RAIN
                : ModConfigs.MESSAGES_BOT_TIME_CLEAR;
        String response = Templates.format(ModConfigs.MESSAGES_BOT_TIME,
                TimeConverter.timeOfDayToHoursMinutes2(time), weather, time / 24000L);
        event.reply(response).setEphemeral(event.getOption("ephemeral", false, OptionMapping::getAsBoolean)).queue();
    }
}
