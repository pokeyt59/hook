package com.github.pinmacaroon.dchook.bot.event;

import com.github.pinmacaroon.dchook.Hook;
import com.github.pinmacaroon.dchook.bot.Bot;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class ReadyEventListener extends ListenerAdapter {

    public ReadyEventListener(Bot bot){
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        Hook.LOGGER.info("Logged in as {}!", event.getJDA().getSelfUser().getName());
    }
}
