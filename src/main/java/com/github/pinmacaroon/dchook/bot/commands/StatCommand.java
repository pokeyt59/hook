package com.github.pinmacaroon.dchook.bot.commands;

import com.github.pinmacaroon.dchook.Hook;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.level.gamerules.GameRules;

import java.math.BigDecimal;

public class StatCommand {
    public static void run(SlashCommandInteractionEvent event) {

        String list = "Stats of this instance:\n```" +
                "uptime = %dmin%n".formatted(Hook.getGameServer().getTickCount() / 20 / 60) +
                "keepinventory = %b%n".formatted(Hook.getGameServer().getGameRules().get(GameRules.KEEP_INVENTORY)) +
                "mcversion = %s%n".formatted(Hook.getGameServer().getServerVersion()) +
                "fabricmcversion = %s%n".formatted(FabricLoader.getInstance().getModContainer("fabricloader")
                        .map(mod -> mod.getMetadata().getVersion().getFriendlyString()).orElse("unknown")) +
                "dchookversion = %s%n".formatted(Hook.VERSION) +
                "players = %d/%d%n".formatted(Hook.getGameServer().getPlayerCount(),
                        Hook.getGameServer().getMaxPlayers()) +
                "memory = ~%smb%n".formatted(Math.rint(
                        (double) (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024
                )) +
                "overworld_border = %s%n".formatted(
                        BigDecimal.valueOf((int) Hook.getGameServer().overworld().getWorldBorder().getSize() / 2)
                                .toPlainString()
                ) +
                "```";
        event.reply(list).setEphemeral(event.getOption("ephemeral", false, OptionMapping::getAsBoolean))
                .queue();
    }
}
