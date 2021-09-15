package lilmayu.uzlabinacek.commands;

import lilmayu.mayusjdautilities.commands.MayuCommand;
import lilmayu.mayusjdautilities.data.MayuCoreData;
import lilmayu.mayusjdautilities.utils.DiscordUtils;
import lilmayu.uzlabinacek.other.Constants;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public class AboutCommand extends MayuCommand {

    public AboutCommand() {
        this.name = "about";

        this.syntax = "about";
        this.description = "Information about this bot";
        this.examples = new String[]{"about"};

        this.guildOnly = false;

        MayuCoreData.registerCommand(this);
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.deferReply(true).complete();

        EmbedBuilder embedBuilder = DiscordUtils.getDefaultEmbed();

        embedBuilder.setTitle("Úžlabiňáček 2.0");
        embedBuilder.setDescription("I am created by [Mayuna](https://lilmayu.tech/)! More description coming soon:tm:...");
        embedBuilder.addField("Current Version", Constants.VERSION, false);
        embedBuilder.addField("Source code",
                "I am open-source! You can find me at [GitHub](https://github.com/lilmayu/Uzlabinacek2). Feel free to contribute or report any issue ;)",
                false);
        embedBuilder.addField("Donations", "If you'd like to donate, you can! [My PayPal link](https://paypal.me/uwulilmayu)", false);

        event.getHook().editOriginalEmbeds(embedBuilder.build()).complete();
    }
}
