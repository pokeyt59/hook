package com.github.pinmacaroon.dchook.bot;

import com.github.pinmacaroon.dchook.bot.event.MessageReceivedListener;
import com.github.pinmacaroon.dchook.bot.event.ReadyEventListener;
import com.github.pinmacaroon.dchook.bot.event.SlashCommandInteractionListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import java.util.EnumSet;

public class Bot {
    private final JDA JDA;
    private long GUILD_ID;
    private long CHANNEL_ID;

    public Bot(String token) {
        net.dv8tion.jda.api.JDA jda;
        jda = JDABuilder.createLight(token, EnumSet.of(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT))
                .addEventListeners(new ReadyEventListener(this))
                .addEventListeners(new MessageReceivedListener(this))
                .addEventListeners(new SlashCommandInteractionListener(this))
                .build();
        try {
            jda.awaitReady();
        } catch (InterruptedException e) {
            this.JDA = null;
            return;
        }
        this.JDA = jda;

        this.JDA.getPresence().setActivity(Activity.of(Activity.ActivityType.WATCHING,
                "over this server (literally 1984)"));

        CommandListUpdateAction commands = this.JDA.updateCommands().addCommands(
                Commands.slash("time", "Check time and weather in the overworld")
                        .addOptions(new OptionData(
                                        OptionType.BOOLEAN, "ephemeral", "Should the message be only visible to you?"
                                ).setRequired(false)
                        )
                        .setContexts(InteractionContextType.GUILD)
                        .setIntegrationTypes(IntegrationType.GUILD_INSTALL),

                Commands.slash("mods", "Check what mods are in the server, if any")
                        .addOptions(new OptionData(
                                        OptionType.BOOLEAN, "ephemeral", "Should the message be only visible to you?"
                                ).setRequired(false),
                                new OptionData(
                                        OptionType.BOOLEAN, "full", "List every mod, even if it takes multiple messages"
                                ).setRequired(false)
                        )
                        .setContexts(InteractionContextType.GUILD)
                        .setIntegrationTypes(IntegrationType.GUILD_INSTALL),

                Commands.slash("list", "List online players")
                        .addOptions(new OptionData(
                                        OptionType.BOOLEAN, "ephemeral", "Should the message be only visible to you?"
                                ).setRequired(false)
                        )
                        .setContexts(InteractionContextType.GUILD)
                        .setIntegrationTypes(IntegrationType.GUILD_INSTALL),

                Commands.slash("stat", "See some stats about the server")
                        .addOptions(new OptionData(
                                        OptionType.BOOLEAN, "ephemeral", "Should the message be only visible to you?"
                                ).setRequired(false)
                        )
                        .setContexts(InteractionContextType.GUILD)
                        .setIntegrationTypes(IntegrationType.GUILD_INSTALL),

                Commands.slash("about", "Get some info about the integration")
                        .addOptions(new OptionData(
                                        OptionType.BOOLEAN, "ephemeral", "Should the message be only visible to you?"
                                ).setRequired(false)
                        )
                        .setContexts(InteractionContextType.GUILD)
                        .setIntegrationTypes(IntegrationType.GUILD_INSTALL)
        );

        commands.queue();
    }

    public JDA getJDA() {
        return JDA;
    }

    public void stop() {
        this.JDA.shutdown();
    }

    public SelfUser getSelfUser() {
        return this.JDA.getSelfUser();
    }

    public long getGUILD_ID() {
        return GUILD_ID;
    }

    public void setGUILD_ID(long GUILD_ID) {
        this.GUILD_ID = GUILD_ID;
    }

    public long getCHANNEL_ID() {
        return CHANNEL_ID;
    }

    public void setCHANNEL_ID(long CHANNEL_ID) {
        this.CHANNEL_ID = CHANNEL_ID;
    }
}
