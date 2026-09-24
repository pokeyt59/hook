package com.github.pinmacaroon.dchook.util;

import com.github.pinmacaroon.dchook.Hook;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class PromotionProvider {
    private static JsonObject mcfetchEmbed = new JsonObject();
    private static JsonObject skinfetchEmbed = new JsonObject();
    private static JsonObject sequoiaEmbed = new JsonObject();
    private static JsonObject oocTip = new JsonObject();
    private static JsonObject docTip = new JsonObject();
    private static JsonObject hydrationTip = new JsonObject();
    private static JsonObject carTip = new JsonObject();

    static {
        mcfetchEmbed.addProperty("url", "https://pinmacaroon.github.io/mcfetch/legacy/index.html");
        mcfetchEmbed.addProperty("type", "rich");
        mcfetchEmbed.addProperty("title", "mcfetch: see minecraft server stats without starting the game!");
        mcfetchEmbed.addProperty("description", """
                                a **simple**, **easy to use** and **minimal tool** to see who are online on your \
                                favorite servers! **no download** needed, **no login** needed! **no ads** and no \
                                restrictions! all you need to get server information is the **IP** address of the \
                                server!""");
        mcfetchEmbed.addProperty("color", 15448355);
        JsonArray mcfetchFlist = new JsonArray();
        JsonObject mcfetchField1 = new JsonObject();
        mcfetchField1.addProperty("value", """
                start using it right now at this link: <https://pinmacaroon.github.io/mcfetch/legacy/index.html>""");
        mcfetchField1.addProperty("name", "interested?");
        mcfetchFlist.add(mcfetchField1);
        mcfetchEmbed.add("fields", mcfetchFlist);
        JsonObject mcfetchAuthor = new JsonObject();
        mcfetchAuthor.addProperty("name", "mcfetch");
        mcfetchAuthor.addProperty("url", "https://pinmacaroon.github.io/mcfetch/legacy/index.html");
        mcfetchEmbed.add("author", mcfetchAuthor);
        mcfetchEmbed.addProperty("timestamp",
                ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)
        );
        JsonObject mcfetchFooter = new JsonObject();
        mcfetchFooter.addProperty("text", """
                this was a promotion of first-party services or a tip message! \
                if you wish to turn this off, go to the configuration file!""");
        mcfetchEmbed.add("footer", mcfetchFooter);



        skinfetchEmbed.addProperty("url", "https://pinmacaroon.github.io/skinfetch/index.html");
        skinfetchEmbed.addProperty("type", "rich");
        skinfetchEmbed.addProperty("title", "skinfetch: quickly take a look at one's current skin");
        skinfetchEmbed.addProperty("description", """
                a small and simple web tool to quickly get a view of a user's skin "from multiple angles"! choose from \
                multiple modes, including but not limited to bust, body, head, skin! toggle the outer layer if you \
                wish! choose a size (in pixels) and boom! just click on it and download the image!""");
        skinfetchEmbed.addProperty("color", 15448355);

        JsonArray skinfetchfields = new JsonArray();
        JsonObject skinfetchfield1 = new JsonObject();
        skinfetchfield1.addProperty("value", """
                gaze upon your fabulous look from your browser now at this link: \
                <https://pinmacaroon.github.io/skinfetch/index.html>""");
        skinfetchfield1.addProperty("name", "interested?");
        skinfetchfields.add(skinfetchfield1);
        skinfetchEmbed.add("fields", skinfetchfields);

        JsonObject skinfetchauthor = new JsonObject();
        skinfetchauthor.addProperty("name", "skinfetch");
        skinfetchauthor.addProperty("url", "https://pinmacaroon.github.io/skinfetch/index.html");
        skinfetchEmbed.add("author", skinfetchauthor);

        skinfetchEmbed.addProperty("timestamp", ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT));

        JsonObject skinfetchfooter = new JsonObject();
        skinfetchfooter.addProperty("text", """
                this was a promotion of first-party services or a tip message! \
                if you wish to turn this off, go to the configuration file!""");
        skinfetchEmbed.add("footer", skinfetchfooter);



        sequoiaEmbed.addProperty("url", "https://pinmacaroon.github.io/mcfetch/legacy/index.html");
        sequoiaEmbed.addProperty("type", "rich");
        sequoiaEmbed.addProperty("title", "Sequoia: Adds a really nice biome with some really tall trees!");
        sequoiaEmbed.addProperty("description", """
                A mod that adds the amazing sequoia forests! Custom \
                tree types, blocks, structures!""");
        sequoiaEmbed.addProperty("color", 15448355);
        JsonArray sequoiafields = new JsonArray();
        JsonObject sequoiafield1 = new JsonObject();
        sequoiafield1.addProperty("value", """
                Full block set like you would expect:
                * Log and Stripped Log
                * Wood and Stripped Wood
                * Planks
                * etc...""");
        sequoiafield1.addProperty("name", "Sequoia wood type");
        sequoiafields.add(sequoiafield1);
        JsonObject sequoiafield2 = new JsonObject();
        sequoiafield2.addProperty("value", """
                Fresh air, big trees. Really beautiful place to set up camp. \
                You can even find berry bushes! You can find three types of sequoia trees:
                * small
                * medium size
                * huge (structure, to be added)""");
        sequoiafield2.addProperty("name", "Sequoia forest");
        sequoiafields.add(sequoiafield2);
        sequoiaEmbed.add("fields", sequoiafields);
        JsonObject sequoiaauthor = new JsonObject();
        sequoiaauthor.addProperty("name", "Sequoia");
        sequoiaauthor.addProperty("url", "https://modrinth.com/mod/sequoia-pine");
        sequoiaauthor.addProperty("icon_url",
                "https://cdn.modrinth.com/data/GYYIncFH/2fabee293b4be237825df51defe3b977058d31e7.png");
        sequoiaEmbed.add("author", sequoiaauthor);
        JsonObject sequoiaimage = new JsonObject();
        sequoiaimage.addProperty("url",
                "https://cdn.modrinth.com/data/GYYIncFH/images/5dd0848d40469d0dd4767c73ac926e112978645d.jpeg");
        sequoiaEmbed.add("image", sequoiaimage);
        sequoiaEmbed.addProperty("timestamp", ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT));
        JsonObject sequoiafooter = new JsonObject();
        sequoiafooter.addProperty("text", """
                this was a promotion of first-party services or a tip message! \
                if you wish to turn this off, go to the configuration file!""");
        sequoiaEmbed.add("footer", sequoiafooter);



        oocTip.addProperty("type", "rich");
        oocTip.addProperty("title", "out of character messages");
        oocTip.addProperty("description", """
                            make messages hidden from the mod by ending it with `//`! it wont get transferred to \
                            Discord! you can configure this in the config file!""");
        JsonObject oocauthor = new JsonObject();
        oocauthor.addProperty("name", "tips and hints");
        oocTip.add("author", oocauthor);
        oocTip.addProperty("timestamp", ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT));
        JsonObject oocfooter = new JsonObject();
        oocfooter.addProperty("text", """
                this was a promotion of first-party services or a tip message! \
                if you wish to turn this off, go to the configuration file!""");
        oocTip.add("footer", oocfooter);



        docTip.addProperty("type", "rich");
        docTip.addProperty("title", "documentation");
        docTip.addProperty("url", "https://pinmacaroon.github.io/hook/docs.html");
        docTip.addProperty("description", """
                oh noes! you couldn't figure out how to use the mod? i got your back! go to\
                 <https://pinmacaroon.github.io/hook/docs.html> and see the table with the configuration keys and\
                 values! this page will be your hub for updates and information concerning the mod.""");
        JsonObject docauthor = new JsonObject();
        docauthor.addProperty("name", "tips and hints");
        docTip.add("author", docauthor);
        docTip.addProperty("timestamp", ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT));
        JsonObject docimage = new JsonObject();
        docimage.addProperty("url", "https://c.tenor.com/BRBnJitMZBYAAAAd/tenor.gif");
        docTip.add("image", docimage);
        JsonObject docfooter = new JsonObject();
        docfooter.addProperty("text", """
                this was a promotion of first-party services or a tip message! \
                if you wish to turn this off, go to the configuration file!""");
        docTip.add("footer", docfooter);



        hydrationTip.addProperty("type", "rich");
        hydrationTip.addProperty("title", "hydration");
        hydrationTip.addProperty("description", """
                **did you drink water today? do you drink enough?**
                an average daily "total water"\
                 intake of 3 liters is recommended by the USDA (this includes water from non-drinking water sources,\
                 like foods). specifically, **eight cups of drinking water a day** is the amount recommended by most\
                 nutritionists.""");
        JsonObject hydrauthor = new JsonObject();
        hydrauthor.addProperty("name", "tips and hints");
        hydrationTip.add("author", hydrauthor);
        hydrationTip.addProperty("timestamp", ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT));
        JsonObject hydrfooter = new JsonObject();
        hydrfooter.addProperty("text", """
                this was a promotion of first-party services or a tip message! \
                if you wish to turn this off, go to the configuration file!""");
        hydrationTip.add("footer", hydrfooter);

        carTip.addProperty("type", "rich");
        JsonObject carimage = new JsonObject();
        carimage.addProperty("url",
                "https://raw.githubusercontent.com/pinmacaroon/hook/refs/heads/master/kocsi.gif");
        carTip.add("image", carimage);
        carTip.addProperty("timestamp", ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT));
        JsonObject carfooter = new JsonObject();
        carfooter.addProperty("text", """
                this was a promotion of first-party services or a tip message! \
                if you wish to turn this off, go to the configuration file!""");
        carTip.add("footer", carfooter);
    }

    /**
     * @param embeds {@link JsonArray} of {@link JsonObject} which are discord embeds
     */
    private static void sendPromotionMessageAPIRequest(JsonArray embeds){
        JsonObject request_body = new JsonObject();
        request_body.addProperty("username", "promotion");
        request_body.add("embeds", embeds);
        Webhook.send(request_body);
    }

    public static void sendPromotion(){
        JsonObject promotion = switch (Hook.RANDOM.nextInt(0, 10)) {
            case 0 -> mcfetchEmbed;
            case 1 -> skinfetchEmbed;
            case 2 -> sequoiaEmbed;
            case 3 -> oocTip;
            case 4 -> docTip;
            case 5 -> hydrationTip;
            case 6 -> carTip;
            default -> null;
        };
        if(promotion == null) return;
        JsonArray promotions = new JsonArray();
        promotions.add(promotion);
        sendPromotionMessageAPIRequest(promotions);
    }
}
