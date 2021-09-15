package lilmayu.uzlabinacek.other;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import lombok.Getter;

public class CommandHistoryEntry {

    private final @Getter CommandEvent event;
    private final @Getter Command command;
    private final @Getter long time;

    public CommandHistoryEntry(CommandEvent event, Command command, long time) {
        this.event = event;
        this.command = command;
        this.time = time;
    }

    public boolean is(CommandEvent event) {
        return this.event.getResponseNumber() == event.getResponseNumber();
    }
}
