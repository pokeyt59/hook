package com.github.pinmacaroon.dchook.bot.commands;

import com.github.pinmacaroon.dchook.Hook;
import com.github.pinmacaroon.dchook.conf.ModConfigs;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.text.MessageFormat;

public class ListCommand {
    public static void run(SlashCommandInteractionEvent event) {
        StringBuilder message = new StringBuilder();
        message.append(MessageFormat.format(
                ModConfigs.MESSAGES_BOT_LIST,
                Hook.getGameServer().getPlayerCount(),
                Hook.getGameServer().getMaxPlayers()
        ));
        Hook.getGameServer().getPlayerList().getPlayers().forEach(
                serverPlayer -> message.append("`").append(serverPlayer.getName().getString()).append("` ")
        );
        event.reply(message.toString()).setEphemeral(event.getOption("ephemeral", false, OptionMapping::getAsBoolean))
                .queue();
    }
}
