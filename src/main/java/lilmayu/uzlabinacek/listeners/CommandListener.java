package lilmayu.uzlabinacek.listeners;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import lilmayu.mayusjdautilities.utils.MessageUtils;
import lilmayu.mayuslibrary.exceptionreporting.ExceptionReporter;
import lilmayu.mayuslibrary.logging.Logger;
import lilmayu.uzlabinacek.other.CommandHistoryEntry;
import lilmayu.uzlabinacek.other.types.CommandLogType;
import lombok.Getter;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.List;

public class CommandListener implements com.jagrosh.jdautilities.command.CommandListener {

    private static @Getter List<CommandHistoryEntry> commandHistoryEntries = new ArrayList<>();

    @Override
    public void onCommand(CommandEvent event, Command command) {
        Logger.custom(new CommandLogType(), "## Command @ " + event.getResponseNumber());
        Logger.custom(new CommandLogType(), "# Name: '" + command.getName() + "'");
        Logger.custom(new CommandLogType(), "# Arguments: '" + event.getArgs() + "'");
        Logger.custom(new CommandLogType(), "# Full message: '" + event.getMessage().getContentRaw() + "'");
        Logger.custom(new CommandLogType(), "# Author: '" + event.getAuthor() + "'");
        Logger.custom(new CommandLogType(), "# Is guild: " + event.isFromType(ChannelType.TEXT));
        if (event.isFromType(ChannelType.TEXT)) {
            Logger.custom(new CommandLogType(), "# Guild: '" + event.getGuild() + "'");
            Logger.custom(new CommandLogType(), "# MessageChannel: '" + event.getChannel() + "'");
        }

        commandHistoryEntries.add(new CommandHistoryEntry(event, command, System.currentTimeMillis()));
    }

    @Override
    public void onCompletedCommand(CommandEvent event, Command command) {
        Logger.custom(new CommandLogType(), "## Command Completed @ " + event.getResponseNumber());
        Logger.custom(new CommandLogType(), "# Data: '" + event.getMessage().getContentRaw() + "'");
        Logger.custom(new CommandLogType(), "# Took " + getTimeForCommand(event) + "ms");
    }

    @Override
    public void onTerminatedCommand(CommandEvent event, Command command) {
        Logger.custom(new CommandLogType(), "## Command Terminated @ " + event.getResponseNumber());
        Logger.custom(new CommandLogType(), "# Data: '" + event.getMessage().getContentRaw() + "'");
        Logger.custom(new CommandLogType(), "# Took " + getTimeForCommand(event) + "ms");
    }

    @Override
    public void onCommandException(CommandEvent event, Command command, Throwable throwable) {
        Logger.warning("## Command Exception @ " + event.getResponseNumber());
        Logger.warning("# Data: '" + event.getMessage().getContentRaw() + "'");
        Logger.warning("# Took " + getTimeForCommand(event) + "ms");
        Logger.warning("# -> Please, see errors below.");

        MessageUtils.Builder.create()
                .setType(MessageUtils.Builder.Type.ERROR)
                .setClosable(true)
                .setEmbed(true)
                .setDeleteAfter(10)
                .setContent("Exception occurred while processing " + event.getAuthor().getAsMention() + "'s command `" + event.getMessage().getContentRaw() + "`! This error will be automatically reported.\n```" + throwable + "```")
                .send(event.getChannel());

        ExceptionReporter.getInstance().uncaughtException(Thread.currentThread(), throwable);
    }

    @Override
    public void onNonCommandMessage(MessageReceivedEvent event) {
        //Main.getMessageWaiterManager().processEvent(event);
    }

    synchronized private long getTimeForCommand(CommandEvent event) {
        for (CommandHistoryEntry entry : commandHistoryEntries) {
            if (entry.is(event)) {
                commandHistoryEntries.remove(entry);
                return System.currentTimeMillis() - entry.getTime();
            }
        }

        return -1;
    }
}
