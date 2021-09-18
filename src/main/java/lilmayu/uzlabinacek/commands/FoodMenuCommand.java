package lilmayu.uzlabinacek.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import lilmayu.mayusjdautilities.commands.MayuCommand;
import lilmayu.mayusjdautilities.data.MayuCoreData;
import lilmayu.mayusjdautilities.utils.MessageUtils;
import lilmayu.uzlabinacek.managers.FoodMenuManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public class FoodMenuCommand extends MayuCommand {

    public FoodMenuCommand() {
        this.name = "foodmenu";
        this.help = "Allows you to managed Food menu in Text Channel";

        this.syntax = "foodmenu <create|remove|update|movedown>";
        this.description = "Allows you to manage Food menu in Text Channel";
        this.examples = new String[]{"foodmenu create", "foodmenu remove", "foodmenu update", "foodmenu movedown"};

        this.children = new SlashCommand[]{new Create(), new Remove(), new Update(), new MoveDown()};

        this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};

        MayuCoreData.registerCommand(this);
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.deferReply(true).complete();

        event.getHook().editOriginalEmbeds(MessageUtils.errorEmbed("Invalid syntax! Please, see `/help foodmenu` for more information.").build()).complete();
    }

    private static class Create extends MayuCommand {

        public Create() {
            this.name = "create";
            this.help = "Creates Food menu in current Text Channel if there is not one already";

            this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            event.deferReply(true).complete();

            TextChannel textChannel = event.getTextChannel();

            if (FoodMenuManager.isManagedMessageInTextChannel(textChannel)) {
                event.getHook().editOriginalEmbeds(MessageUtils.errorEmbed("There is already Food menu! Use command `/foodmenu movedown` to move it down.").build()).complete();
                return;
            }

            FoodMenuManager.createFoodMenuMessage(textChannel);
            event.getHook().editOriginalEmbeds(MessageUtils.successfulEmbed("Food menu successfully created.").build()).complete();
            FoodMenuManager.save();
        }
    }

    private static class Remove extends MayuCommand {

        public Remove() {
            this.name = "remove";
            this.help = "Removes Food menu in current Text Channel if there is one";

            this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            event.deferReply(true).complete();

            TextChannel textChannel = event.getTextChannel();

            if (!FoodMenuManager.isManagedMessageInTextChannel(textChannel)) {
                event.getHook().editOriginalEmbeds(MessageUtils.errorEmbed("There is no Food menu! Use command `/foodmenu create` to create one.").build()).complete();
                return;
            }

            if (FoodMenuManager.removeFoodMenuMessage(textChannel)) {
                event.getHook().editOriginalEmbeds(MessageUtils.successfulEmbed("Successfully removed Food menu from current Text Channel!").build()).complete();
                FoodMenuManager.save();
            } else {
                event.getHook()
                        .editOriginalEmbeds(MessageUtils.errorEmbed("There was an error while removing Food menu from current Text Channel! You will have to remove it yourself.")
                                .build())
                        .complete();
            }
        }
    }

    private static class Update extends MayuCommand {

        public Update() {
            this.name = "update";
            this.help = "Updates Food menu in current Text Channel if there is one";

            this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            event.deferReply(true).complete();

            TextChannel textChannel = event.getTextChannel();

            if (!FoodMenuManager.isManagedMessageInTextChannel(textChannel)) {
                event.getHook().editOriginalEmbeds(MessageUtils.errorEmbed("There is no Food menu! Use command `/foodmenu create` to create one.").build()).complete();
                return;
            }

            if (FoodMenuManager.updateManagedMessage(textChannel)) {
                event.getHook().editOriginalEmbeds(MessageUtils.successfulEmbed("Successfully updated Food menu in current Text Channel!").build()).complete();
            } else {
                event.getHook().editOriginalEmbeds(MessageUtils.errorEmbed("There was error while updating Food menu in current Text Channel!").build()).complete();
            }
        }
    }

    private static class MoveDown extends MayuCommand {

        public MoveDown() {
            this.name = "movedown";
            this.help = "Moves down Food menu in current Text Channel if there is one";

            this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        }


        @Override
        protected void execute(SlashCommandEvent event) {
            event.deferReply(true).complete();

            TextChannel textChannel = event.getTextChannel();

            FoodMenuManager.moveDownManagedMessage(textChannel);
            event.getHook().editOriginalEmbeds(MessageUtils.successfulEmbed("Successfully moved down Food menu in current Text Channel!").build()).complete();
        }
    }
}
