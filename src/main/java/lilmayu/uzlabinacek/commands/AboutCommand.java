package lilmayu.uzlabinacek.commands;

import lilmayu.mayusjdautilities.commands.MayuCommand;
import lilmayu.mayusjdautilities.data.MayuCoreData;
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
        event.getInteraction().deferReply(true).complete();
        event.getHook().editOriginal("About me!!!").complete();
    }
}
