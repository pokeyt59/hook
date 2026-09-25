# Changelog

## 1.4.0

For Minecraft 26.2 (Fabric Loader 0.19.3+, Java 25).

### New
- **Emojis both ways:** emojis from Discord show as `:name:` in game (Minecraft can't draw most of them), including
  custom emojis and ones sent from other servers (e.g. Vencord FakeNitro links). `:name:` typed in game becomes the
  emoji on Discord. Turn off with `functions.emoji.shortcodes=false`.
- **Ping from Minecraft:** `@name` in game pings that Discord member (username, server nickname or display name).
  @everyone, @here and roles never ping. Turn off with `functions.bot.pings=false`.
- **Configurable `/time` reply:** `messages.bot.time` (`{0}` time, `{1}` weather, `{2}` day) and the weather words
  `messages.bot.time.clear` / `.rain` / `.thunder`.
- **Discord messages in game:** server nicknames, clickable `[image: …]` / `[video: …]` / `[file: …]` labels,
  `[sticker: …]`, forwarded messages, and readable Discord timestamps (`functions.timestamp.zone`).
- **Bot status** is configurable (`functions.bot.status.type` / `.text`).
- **Setup diagnostics:** the log says exactly why Discord messages can't reach the game (bot not in the server, with
  an invite link; channel not visible; missing View Channel permission).

### Fixed
- `@everyone` / `@here` typed in game no longer shows up as a ping on Discord.
- A reply to a deleted message, or a forwarded message, no longer gets lost.
- Configurable messages keep apostrophes (`It's`) and don't group numbers (waypoints showed `1,500`).
- The `/time` reply no longer ends in "!!" during thunderstorms.

### Improved
- The server no longer waits for Discord at startup (about 2 s faster), and a Discord outage can't crash it.
- Messages to Discord never block the game, arrive in order, are merged when many come at once, and wait out
  Discord's rate limits instead of getting lost.
- New settings are added to your existing `dchook.properties` on update (the log lists them).
- The download is about 45% smaller (12 MB → 6.8 MB).
