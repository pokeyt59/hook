package com.github.pinmacaroon.dchook.bot;

import com.github.pinmacaroon.dchook.Hook;
import com.github.pinmacaroon.dchook.bot.event.MessageReceivedListener;
import com.github.pinmacaroon.dchook.bot.event.ReadyEventListener;
import com.github.pinmacaroon.dchook.bot.event.SlashCommandInteractionListener;
import com.github.pinmacaroon.dchook.conf.ModConfigs;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.messages.MessageRequest;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Bot {
    private final JDA JDA;
    private long GUILD_ID;
    private long CHANNEL_ID;

    public Bot(String token) {
        // replies echo player and mod names, never let them turn into pings
        MessageRequest.setDefaultMentions(EnumSet.noneOf(Message.MentionType.class));
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

        this.JDA.getPresence().setActivity(configuredActivity());

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

    private static Activity configuredActivity() {
        String text = ModConfigs.FUNCTIONS_BOT_STATUS_TEXT;
        Activity.ActivityType type = switch (ModConfigs.FUNCTIONS_BOT_STATUS_TYPE.strip().toLowerCase()) {
            case "none" -> null;
            case "playing" -> Activity.ActivityType.PLAYING;
            case "watching" -> Activity.ActivityType.WATCHING;
            case "listening" -> Activity.ActivityType.LISTENING;
            case "competing" -> Activity.ActivityType.COMPETING;
            case "custom" -> Activity.ActivityType.CUSTOM_STATUS;
            default -> {
                Hook.LOGGER.warn("unknown bot status type '{}', using watching", ModConfigs.FUNCTIONS_BOT_STATUS_TYPE);
                yield Activity.ActivityType.WATCHING;
            }
        };
        return (type == null || text.isBlank()) ? null : Activity.of(type, text);
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

    /**
     * Logs whether the bot can read the chat channel, discord sends no messages from channels it can't see.
     */
    public void checkChannel() {
        if (this.JDA == null) return;
        Guild guild = this.JDA.getGuildById(GUILD_ID);
        if (guild == null) {
            String invite = this.JDA.getInviteUrl(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND,
                    Permission.MESSAGE_HISTORY);
            if (!invite.contains("applications.commands")) invite = invite.replace("scope=bot", "scope=bot+applications.commands");
            Hook.LOGGER.error("the bot isn't in the webhook's discord server ({}), messages from discord won't reach the"
                    + " game! invite it with this link: {} (servers the bot is in: {})", GUILD_ID, invite,
                    this.JDA.getGuilds().isEmpty() ? "none" : this.JDA.getGuilds().stream()
                            .map(g -> g.getName() + " (" + g.getId() + ")").collect(Collectors.joining(", ")));
            return;
        }
        GuildChannel channel = guild.getGuildChannelById(CHANNEL_ID);
        if (channel == null) {
            Hook.LOGGER.error("the bot is in {}, but can't find the webhook's channel ({}) there, messages from discord"
                    + " won't reach the game! give the bot the View Channel permission in it (check the category too)."
                    + " channels the bot can see: {}", guild.getName(), CHANNEL_ID, guild.getTextChannels().stream()
                    .limit(20).map(c -> "#" + c.getName()).collect(Collectors.joining(", ")));
            return;
        }
        if (!guild.getSelfMember().hasAccess(channel)) {
            Hook.LOGGER.error("the bot has no View Channel permission in #{}, messages from discord won't reach the"
                    + " game! allow it for the bot or its role (check the category too)", channel.getName());
            return;
        }
        Hook.LOGGER.info("relaying messages from #{} to the game", channel.getName());
    }

    private record CachedMember(Long id, long expires) {}

    private final Map<String, CachedMember> memberCache = new ConcurrentHashMap<>();
    private volatile boolean memberSearchFailed = false;

    /**
     * Finds a member of the chat's server by username, server nickname or display name (ignoring case), through
     * discord's member search so no extra intent is needed. Answers are cached for a while, misses too.
     *
     * @return the user id, or null if nobody has that name
     */
    public Long findMemberId(String name) {
        String key = name.toLowerCase(Locale.ROOT);
        CachedMember cached = memberCache.get(key);
        if (cached != null && cached.expires() > System.currentTimeMillis()) return cached.id();

        Long id = searchMember(key);
        long ttl = id != null ? 10 * 60_000 : 60_000;
        memberCache.put(key, new CachedMember(id, System.currentTimeMillis() + ttl));
        return id;
    }

    private Long searchMember(String name) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create("https://discord.com/api/v10/guilds/" + GUILD_ID + "/members/search?limit=10&query="
                            + URLEncoder.encode(name, StandardCharsets.UTF_8)))
                    .header("Authorization", "Bot " + ModConfigs.FUNCTIONS_BOT_TOKEN.strip())
                    .timeout(Duration.ofSeconds(5))
                    .build();
            HttpResponse<String> response = Hook.HTTPCLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                if (!memberSearchFailed) {
                    memberSearchFailed = true;
                    Hook.LOGGER.warn("couldn't look up discord members for @pings (status {}): {}",
                            response.statusCode(), response.body());
                }
                return null;
            }
            Long byNick = null, byDisplayName = null;
            for (JsonElement element : JsonParser.parseString(response.body()).getAsJsonArray()) {
                JsonObject member = element.getAsJsonObject();
                JsonObject user = member.getAsJsonObject("user");
                long id = user.get("id").getAsLong();
                if (name.equalsIgnoreCase(string(user, "username"))) return id;
                if (byNick == null && name.equalsIgnoreCase(string(member, "nick"))) byNick = id;
                if (byDisplayName == null && name.equalsIgnoreCase(string(user, "global_name"))) byDisplayName = id;
            }
            return byNick != null ? byNick : byDisplayName;
        } catch (Exception e) {
            Hook.LOGGER.warn("couldn't look up the discord member '{}' for a @ping: {}", name, e.toString());
            return null;
        }
    }

    private static String string(JsonObject object, String key) {
        JsonElement value = object.get(key);
        return value == null || value.isJsonNull() ? null : value.getAsString();
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
