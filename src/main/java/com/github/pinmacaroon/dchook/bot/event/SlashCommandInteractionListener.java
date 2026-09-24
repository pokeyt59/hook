package com.github.pinmacaroon.dchook.bot.event;

import com.github.pinmacaroon.dchook.Hook;
import com.github.pinmacaroon.dchook.bot.Bot;
import com.github.pinmacaroon.dchook.bot.commands.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

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
            case "time" -> onServerThread(event, TimeCommand::run);
            case "mods" -> ModsCommand.run(event);
            case "list" -> onServerThread(event, ListCommand::run);
            case "stat" -> onServerThread(event, StatCommand::run);
            case "about" -> AboutCommand.run(event);
            default -> event.reply("""
                            An internal error occurred! Please send a bug report: \
                            <https://github.com/pinmacaroon/hook/issues>""").setEphemeral(true).queue();
        }
    }

    // game state (players, time, game rules) may only be read on the server thread, not on JDA's
    private static void onServerThread(SlashCommandInteractionEvent event, Consumer<SlashCommandInteractionEvent> command) {
        MinecraftServer server = Hook.getGameServer();
        if (server == null) {
            event.reply("The server is still starting, try again in a moment!").setEphemeral(true).queue();
            return;
        }
        server.execute(() -> {
            try {
                command.accept(event);
            } catch (Exception e) {
                Hook.LOGGER.error("/{} failed", event.getName(), e);
                event.reply("Something went wrong running this command, check the server log.").setEphemeral(true).queue();
            }
        });
    }
}
