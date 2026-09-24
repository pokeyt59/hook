package com.github.pinmacaroon.dchook.bot.event;

import com.github.pinmacaroon.dchook.bot.Bot;
import com.github.pinmacaroon.dchook.bot.commands.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class SlashCommandInteractionListener extends ListenerAdapter {
    private final Bot BOT;

    public SlashCommandInteractionListener(Bot bot) {
        this.BOT = bot;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getGuild() == null) return;
        if (event.getGuild().getIdLong() != this.BOT.getGUILD_ID()) return;
        switch (event.getName()) {
            case "time" -> TimeCommand.run(event);
            case "mods" -> ModsCommand.run(event);
            case "list" -> ListCommand.run(event);
            case "stat" -> StatCommand.run(event);
            case "about" -> AboutCommand.run(event);
            default -> event.reply("""
                            An internal error occurred! Please send a bug report: \
                            <https://github.com/pinmacaroon/hook/issues>""").setEphemeral(true).queue();
        }
    }
}
