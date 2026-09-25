# TODO.md

This file is basically a checklist of things to do in the codebase.

## Chores


## Done

* `@name` in game pings that discord member (username, nickname or display name), @everyone/@here/roles never do (`functions.bot.pings`)
* emojis: 😀 shows as `:grinning:` in game, `:grinning:` typed in game shows as 😀 on discord (`functions.emoji.shortcodes`)
* the `/time` reply and its weather words are configurable (`messages.bot.time*`)
* discord custom emojis (`<:name:id>`) show up as `:name:` in game (JDA's `getContentDisplay` already does it,
  forwarded messages are converted by hand)
* new settings are appended to existing config files on update
* configurable messages keep apostrophes and don't group numbers (waypoints showed `1,500`)
* discord messages in game: server nicknames, attachment/sticker labels, replies to deleted messages and forwarded
  messages no longer get lost

* parse discord timestamps (`<t:...>`) into readable text in game
* `ServerMessageEvents.GAME_MESSAGE`: skip our own discord relays with a flag instead of the `<` check, skip action bar messages
* the webhook and the bot can't mention @everyone, @here, roles or users anymore
* `@everyone`/`@here` typed in game no longer even looks like a ping in discord (zero width space after the `@`)
* the bot's status is configurable (`functions.bot.status.type` / `functions.bot.status.text`)
* performance: webhook messages are sent without blocking the server thread
