# TODO.md

This file is basically a checklist of things to do in the codebase.

## Chores

* make the `/time` command's reply configurable like the other messages
* render discord custom emojis (`<:name:id>`) as `:name:` in game

## Done

* parse discord timestamps (`<t:...>`) into readable text in game
* `ServerMessageEvents.GAME_MESSAGE`: skip our own discord relays with a flag instead of the `<` check, skip action bar messages
* the webhook and the bot can't mention @everyone, @here, roles or users anymore
* the bot's status is configurable (`functions.bot.status.type` / `functions.bot.status.text`)
* performance: webhook messages are sent without blocking the server thread
