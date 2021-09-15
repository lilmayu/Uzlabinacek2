package lilmayu.uzlabinacek;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import lilmayu.mayusjdautilities.commands.MayuHelpCommand;
import lilmayu.mayusjdautilities.data.MayuCoreListener;
import lilmayu.mayusjdautilities.utils.DiscordUtils;
import lilmayu.mayusjdautilities.utils.MessageUtils;
import lilmayu.mayuslibrary.console.colors.Color;
import lilmayu.mayuslibrary.console.colors.Colors;
import lilmayu.mayuslibrary.exceptionreporting.ExceptionListener;
import lilmayu.mayuslibrary.exceptionreporting.ExceptionReporter;
import lilmayu.mayuslibrary.logging.Logger;
import lilmayu.mayuslibrary.logging.coloring.ColoringString;
import lilmayu.uzlabinacek.commands.AboutCommand;
import lilmayu.uzlabinacek.listeners.CommandListener;
import lilmayu.uzlabinacek.other.Config;
import lilmayu.uzlabinacek.other.Constants;
import lilmayu.uzlabinacek.other.types.CommandLogType;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

public class Main {

    // Discord
    private static @Getter JDA jda;
    private static @Getter CommandClientBuilder client;

    public static void main(String[] args) {
        Logger.info("## Úžlabiňáček 2.0 ##");
        Logger.info("# Made by Mayuna (officiallilmayu_#8016)");
        Logger.info("Starting up...");

        long startTime = System.currentTimeMillis();

        Logger.info("Loading library settings...");
        loadLibrarySettings();

        Logger.info("Loading config...");
        Config.load();

        Logger.info("Loading JDA stuff...");
        client = new CommandClientBuilder().useDefaultGame()
                .useHelpBuilder(false)
                .setOwnerId(String.valueOf(Config.getOwnerID()))
                .setActivity(Activity.playing("2.0!"))
                .setPrefix(Config.getPrefix())
                .setAlternativePrefix(Constants.ALTERNATIVE_PREFIX)
                .setPrefixes(new String[]{"<@!872486134209474641>"})
                .setListener(new CommandListener());

        Logger.info("Loading commands...");
        loadCommands();

        Logger.info("Logging into Discord...");
        try {
            JDABuilder jdaBuilder = JDABuilder.createDefault(Config.getToken())
                    .addEventListeners(client.build())
                    .addEventListeners(new MayuCoreListener())
                    .enableIntents(GatewayIntent.GUILD_PRESENCES)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .enableIntents(GatewayIntent.DIRECT_MESSAGES)
                    .enableIntents(GatewayIntent.DIRECT_MESSAGE_REACTIONS)
                    .setMemberCachePolicy(MemberCachePolicy.ALL);
            jda = jdaBuilder.build().awaitReady();
        } catch (Exception exception) {
            exception.printStackTrace();
            Logger.error("Error occurred while logging into Discord! Please, check your bot token in " + Config.CONFIG_PATH + "!");
            System.exit(-1);
        }
        Logger.info("Logged in!");

        Logger.info("Loading managers...");
        loadManagers();

        Logger.info("Loading done! Took " + (System.currentTimeMillis() - startTime) + "ms");

    }

    private static void loadCommands() {
        client.addSlashCommands(new MayuHelpCommand(), new AboutCommand());
    }

    private static void loadManagers() {

    }

    private static void loadLibrarySettings() {
        Logger.setFormat("- " + Logger.getFormat());
        Logger.addColoringString(new ColoringString(new CommandLogType(), new Color().setForeground(Colors.DARK_GRAY).build(), Color.RESET));
        DiscordUtils.setDefaultEmbed(new EmbedBuilder().setFooter("Powered by Úžlabiňáček 2.0").setColor(new java.awt.Color(0x8E1919)));
        ExceptionReporter.registerExceptionReporter();
        ExceptionReporter.getInstance().addListener(new ExceptionListener("default", "lilmayu", exceptionReport -> {
            exceptionReport.getThrowable().printStackTrace();
            Logger.error("Exception occurred! Sending it to Mayo's exception Message channel.");

            if (Main.getJda() != null && Config.getExceptionMessageChannelID() != 0) {
                MessageChannel messageChannel = Main.getJda().getTextChannelById(Config.getExceptionMessageChannelID());
                if (messageChannel != null) {
                    MessageUtils.sendExceptionMessage(messageChannel, exceptionReport.getThrowable());
                } else {
                    Logger.error("Unable to send exception to Exception message channel! (Invalid ExceptionMessageChannelID)");
                }
            } else {
                Logger.error("Unable to send exception to Exception message channel! (JDA is null / ExceptionMessageChannelID is not set)");
            }
        }));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Logger.info("Shutting down...");
            Logger.info("Saving config...");
            Config.save();
            Logger.info("o/");
        }));
    }
}
