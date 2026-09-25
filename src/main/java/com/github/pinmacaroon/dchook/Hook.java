package com.github.pinmacaroon.dchook;

import com.github.pinmacaroon.dchook.bot.Bot;
import com.github.pinmacaroon.dchook.conf.ModConfigs;
import com.github.pinmacaroon.dchook.util.EventListeners;
import com.github.pinmacaroon.dchook.util.Updater;
import com.github.pinmacaroon.dchook.util.Webhook;
import com.github.zafarkhaja.semver.Version;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.regex.Pattern;

public class Hook implements DedicatedServerModInitializer {

    public static final String MOD_ID = "dchook";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final HttpClient HTTPCLIENT = HttpClient.newHttpClient();
    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();
    // taken from fabric.mod.json, which gets it from mod_version in gradle.properties
    public static final Version VERSION = Version.parse(FabricLoader.getInstance().getModContainer(MOD_ID)
            .orElseThrow().getMetadata().getVersion().getFriendlyString());
    public static final String DOCS_URL = "https://modrinth.com/mod/dchook";
    public static final Random RANDOM = new Random(Instant.now().getEpochSecond());
    @SuppressWarnings("RegExpRedundantEscape")
    public static final Pattern WEBHOOK_URL_PATTERN = Pattern.compile(
            "^https:\\/\\/(ptb\\.|canary\\.)?discord\\.com\\/api\\/webhooks\\/\\d+\\/.+$"
    );

    public static URI WEBHOOK_URI;

    public static volatile Bot BOT;
    public static volatile boolean SERVER_STOPPED = false;

    private static MinecraftServer MINECRAFT_SERVER;

    public static MinecraftServer getGameServer() {
        return MINECRAFT_SERVER;
    }

    public static void setMinecraftServer(MinecraftServer minecraftServer) {
        MINECRAFT_SERVER = minecraftServer;
    }

    @Override
    public void onInitializeServer() {
        ModConfigs.registerConfigs();

        if(!ModConfigs.FUNCTIONS_MODENABLED){
            LOGGER.error("hook mod was explicitly told to not operate!");
            return;
        }

        // before the webhook checks, so a broken webhook doesn't keep a fix from being installed
        if(ModConfigs.FUNCTIONS_UPDATE) Updater.start();

        if(!WEBHOOK_URL_PATTERN.matcher(ModConfigs.WEBHOOK_URL).find()){
            LOGGER.error("webhook url was not a valid discord api endpoint, thus the mod cant operate!");
            return;
        }

        WEBHOOK_URI = URI.create(ModConfigs.IS_THREAD
                ? ModConfigs.WEBHOOK_URL + "?thread_id=" + ModConfigs.THREAD_ID
                : ModConfigs.WEBHOOK_URL);

        if(!ModConfigs.FUNCTIONS_PROMOTIONS_ENABLED){
            LOGGER.warn("promotions were disabled by config. please consider turning them back on to support the mod!");
        }

        // messages queue up until the webhook is confirmed, so listeners can go in right away
        EventListeners.registerEventListeners();

        // talking to discord takes a few seconds, the server doesn't have to wait for it
        Thread startup = new Thread(Hook::connectToDiscord, "dchook-startup");
        startup.setDaemon(true);
        startup.start();
    }

    private static void connectToDiscord() {
        JsonObject webhook;
        try {
            HttpRequest get_webhook = HttpRequest.newBuilder()
                    .GET()
                    .uri(WEBHOOK_URI)
                    .timeout(Duration.ofSeconds(15))
                    .build();
            HttpResponse<String> response = HTTPCLIENT.send(get_webhook, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 == 4) {
                LOGGER.error("the webhook was not found! discord said: '{}'", response.body());
                Webhook.markReady(false);
                return;
            }
            if (response.statusCode() != 200) {
                throw new IllegalStateException("discord answered with status " + response.statusCode());
            }
            webhook = JsonParser.parseString(response.body()).getAsJsonObject();
        } catch (Exception e) {
            // most likely discord is down for a moment, sending is still worth trying
            LOGGER.warn("couldn't check the webhook, messages will be sent anyway: {}", e.toString());
            Webhook.markReady(true);
            if (ModConfigs.FUNCTIONS_BOT_ENABLED)
                LOGGER.error("two way chat needs the webhook check to succeed, it's disabled until the next restart");
            return;
        }
        Webhook.markReady(true);

        if (ModConfigs.FUNCTIONS_BOT_ENABLED) {
            try {
                Bot bot = new Bot(ModConfigs.FUNCTIONS_BOT_TOKEN);
                bot.setGUILD_ID(webhook.get("guild_id").getAsLong());
                bot.setCHANNEL_ID(ModConfigs.IS_THREAD
                        ? Long.parseLong(ModConfigs.THREAD_ID)
                        : webhook.get("channel_id").getAsLong());
                BOT = bot;
                // the server may have stopped while the bot was logging in
                if (SERVER_STOPPED) bot.stop();
            } catch (Exception e) {
                LOGGER.error("couldn't initialise bot, two way chat disabled! please check your bot token or send a bug report on github!", e);
            }
        } else {
            LOGGER.info("two way chat has been disabled by the config (botless start)");
        }

        LOGGER.info("all checks succeeded, starting webhook managing! version: {}", VERSION);
    }
}
