package com.github.pinmacaroon.dchook.conf;

import com.github.pinmacaroon.dchook.Hook;
import com.mojang.datafixers.util.Pair;

import java.time.Instant;

public class ModConfigs {
    public static SimpleConfig CONFIG;
    public static String WEBHOOK_URL;
    public static String THREAD_ID;
    public static String MESSAGES_SERVER_STARTING;
    public static String MESSAGES_SERVER_STOPPED;
    public static String MESSAGES_SERVER_STARTED;
    public static String MESSAGES_SERVER_STOPPING;
    public static String MESSAGES_SERVER_WAYPOINT;
    public static String MESSAGES_BOT_LIST;
    public static String MESSAGES_BOT_MODS_LIST;
    public static String MESSAGES_BOT_MODS_NONE;
    public static boolean IS_THREAD;
    public static boolean FUNCTIONS_ALLOWOOCMESSAGES;
    public static boolean MESSAGES_SERVER_STARTING_ALLOWED;
    public static boolean MESSAGES_SERVER_STOPPED_ALLOWED;
    public static boolean MESSAGES_SERVER_STARTED_ALLOWED;
    public static boolean MESSAGES_SERVER_STOPPING_ALLOWED;
    public static boolean FUNCTIONS_MODENABLED;
    public static boolean FUNCTIONS_PROMOTIONS_ENABLED;
    public static boolean FUNCTIONS_BOT_ENABLED;
    public static String FUNCTIONS_BOT_TOKEN;
    public static String FUNCTIONS_BOT_STATUS_TYPE;
    public static String FUNCTIONS_BOT_STATUS_TEXT;
    public static boolean FUNCTIONS_UPDATE;
    public static String FUNCTIONS_UPDATE_CHANNEL;
    public static boolean FUNCTIONS_UPDATE_AUTO;
    public static String FUNCTIONS_UPDATE_REPO;
    private static ModConfigProvider configs;

    public static void registerConfigs() {
        configs = new ModConfigProvider();
        createConfigs();

        CONFIG = SimpleConfig.of(Hook.MOD_ID).provider(configs).request();

        assignConfigs();
    }

    private static void createConfigs() {
        configs.addDocumentationLine("Default config file generated with version " + Hook.VERSION + " at " + Instant.now().toString() + "!");
        configs.addBlankLine();

        configs.addDocumentationLine("Configure functionality of the mod:");
        configs.addKeyValuePair(new Pair<>("functions.mod_enabled", true), "enables/disables the mod's functionality");
        configs.addKeyValuePair(new Pair<>("functions.allow_ooc_messages", true), "allow players to be ignored from proxying if their message ends with double slashes?");
        configs.addKeyValuePair(new Pair<>("functions.promotions.enabled", true), "are tips and hints/promotion embeds allowed to be sent to Discord");
        configs.addKeyValuePair(new Pair<>("functions.bot.enabled", true), "is two-way chat (the bot) enabled?");
        configs.addKeyValuePair(new Pair<>("functions.bot.token", "TOKEN"), "bot token");
        configs.addKeyValuePair(new Pair<>("functions.bot.status.type", "watching"), "bot status type: playing, watching, listening, competing, custom or none");
        configs.addKeyValuePair(new Pair<>("functions.bot.status.text", "over this server (literally 1984)"), "bot status text");
        configs.addKeyValuePair(new Pair<>("functions.update", true), "check for updates");
        configs.addKeyValuePair(new Pair<>("functions.update.channel", "release"), "update channel: release (tagged github releases) or alpha (latest build from github actions, unstable!)");
        configs.addKeyValuePair(new Pair<>("functions.update.auto", false), "download updates automatically and install them when the server stops");
        configs.addKeyValuePair(new Pair<>("functions.update.repo", "pokeyt59/hook"), "github repository (owner/name) to get updates from");
        configs.addBlankLine();

        configs.addDocumentationLine("Configure Discord connection related parameters:");
        configs.addKeyValuePair(new Pair<>("webhook.url", "https://discord.com/api/webhooks/000/ABCDEF"), "url of webhook");
        configs.addDocumentationLine("Configure if webhook points to a thread");
        configs.addKeyValuePair(new Pair<>("webhook.thread", false), "is channel a thread?");
        configs.addDocumentationLine("Configure Thread ID if true. DON'T ADD ?thread_id PARAMETER TO THE WEBHOOK AS THE MOD AUTOMATICALLY INSERTS IT!");
        configs.addKeyValuePair(new Pair<>("webhook.thread.id", "01234567890123456789"), "id of thread");
        configs.addBlankLine();

        configs.addDocumentationLine("Configure messages sent:");
        configs.addKeyValuePair(new Pair<>("messages.server.starting", "The server is starting!"), "start message");
        configs.addKeyValuePair(new Pair<>("messages.server.stopped", "The server has been stopped!"), "stop message");
        configs.addKeyValuePair(new Pair<>("messages.server.started", "The server has started!"), "opened/fully started message");
        configs.addKeyValuePair(new Pair<>("messages.server.stopping", "The server is stopping!"), "stopping message");
        configs.addKeyValuePair(new Pair<>("messages.server.starting.allowed", true), "start message allowed?");
        configs.addKeyValuePair(new Pair<>("messages.server.stopped.allowed", true), "stop message allowed?");
        configs.addKeyValuePair(new Pair<>("messages.server.started.allowed", true), "opened/fully started message allowed?");
        configs.addKeyValuePair(new Pair<>("messages.server.stopping.allowed", true), "stopping message allowed?");
        configs.addKeyValuePair(
                new Pair<>("messages.server.waypoint", "Shared a waypoint called **{0} ({1})** at `{2}, {3}, {4}` from {5}!"),
                "xaeos waypoint message.\n# {0}: waypoint name\n# {1}: waypoint letters\n# {2}, {3} and{4}: x, y and z\n# {5}: dimension name"
        );
        configs.addKeyValuePair(new Pair<>("messages.bot.list", "There are currently **{0}**/{1} players online: "), "the list command preamble message\n# {0}: online players\n# {1}: max players");
        configs.addKeyValuePair(new Pair<>("messages.bot.mods.list", "The server currently has {0} required mods: "), "the mods command preamble message\n# {0}: number of mods");
        configs.addKeyValuePair(new Pair<>("messages.bot.mods.none", "The server currently has no required mods, you can join with a vanilla client!"), "the mods command message when no mods are needed by the client");
        configs.addDocumentationLine("note: the time command cannot be customised yet because im lazy :3");

        configs.addBlankLine();
        configs.addDocumentationLine("Something didn't work? See the documentation or report an issue at this url: <" + Hook.DOCS_URL + ">!");
    }

    private static void assignConfigs() {
        WEBHOOK_URL = CONFIG.getOrDefault("webhook.url", "");
        IS_THREAD = CONFIG.getOrDefault("webhook.thread", false);
        THREAD_ID = CONFIG.getOrDefault("webhook.thread.id", "");
        MESSAGES_SERVER_STARTING = CONFIG.getOrDefault("messages.server.starting", "messages.server.starting");
        MESSAGES_SERVER_STARTED = CONFIG.getOrDefault("messages.server.started", "messages.server.started");
        MESSAGES_SERVER_STOPPED = CONFIG.getOrDefault("messages.server.stopped", "messages.server.stopped");
        MESSAGES_SERVER_STOPPING = CONFIG.getOrDefault("messages.server.stopping", "messages.server.stopping");
        MESSAGES_SERVER_WAYPOINT = CONFIG.getOrDefault("messages.server.waypoint", "messages.server.waypoint");
        MESSAGES_BOT_LIST = CONFIG.getOrDefault("messages.bot.list", "messages.bot.list");
        MESSAGES_BOT_MODS_LIST = CONFIG.getOrDefault("messages.bot.mods.list", "messages.bot.mods.list");
        MESSAGES_BOT_MODS_NONE = CONFIG.getOrDefault("messages.bot.mods.none", "messages.bot.mods.none");
        FUNCTIONS_ALLOWOOCMESSAGES = CONFIG.getOrDefault("functions.allow_ooc_messages", false);

        MESSAGES_SERVER_STARTING_ALLOWED = CONFIG.getOrDefault("messages.server.starting.allowed", false);
        MESSAGES_SERVER_STARTED_ALLOWED = CONFIG.getOrDefault("messages.server.started.allowed", false);
        MESSAGES_SERVER_STOPPED_ALLOWED = CONFIG.getOrDefault("messages.server.stopped.allowed", false);
        MESSAGES_SERVER_STOPPING_ALLOWED = CONFIG.getOrDefault("messages.server.stopping.allowed", false);

        FUNCTIONS_MODENABLED = CONFIG.getOrDefault("functions.mod_enabled", true);
        FUNCTIONS_PROMOTIONS_ENABLED = CONFIG.getOrDefault("functions.promotions.enabled", false);

        FUNCTIONS_BOT_ENABLED = CONFIG.getOrDefault("functions.bot.enabled", false);
        FUNCTIONS_BOT_TOKEN = CONFIG.getOrDefault("functions.bot.token", "");
        FUNCTIONS_BOT_STATUS_TYPE = CONFIG.getOrDefault("functions.bot.status.type", "watching");
        FUNCTIONS_BOT_STATUS_TEXT = CONFIG.getOrDefault("functions.bot.status.text", "over this server (literally 1984)");

        FUNCTIONS_UPDATE = CONFIG.getOrDefault("functions.update", false);
        FUNCTIONS_UPDATE_CHANNEL = CONFIG.getOrDefault("functions.update.channel", "release");
        FUNCTIONS_UPDATE_AUTO = CONFIG.getOrDefault("functions.update.auto", false);
        FUNCTIONS_UPDATE_REPO = CONFIG.getOrDefault("functions.update.repo", "pokeyt59/hook");

        System.out.println("all " + configs.getConfigsList().size() + " have been set properly");
    }
}
