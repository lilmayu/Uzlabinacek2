package lilmayu.uzlabinacek.other;

import com.google.gson.JsonPrimitive;
import lilmayu.mayusjsonutils.JsonUtil;
import lilmayu.mayusjsonutils.objects.MayuJson;
import lilmayu.mayuslibrary.logging.Logger;
import lombok.Getter;
import lombok.Setter;

public class Config {

    public static final String CONFIG_PATH = "./bot_config.json";

    private static @Getter @Setter String prefix = "u!";
    private static @Getter @Setter String token = "### YOUR TOKEN HERE ###";
    private static @Getter @Setter long exceptionMessageChannelID = 0;
    private static @Getter @Setter long ownerID = 0;
    private static @Getter @Setter boolean debug = false;

    public static void load() {
        try {
            MayuJson mayuJson = JsonUtil.createOrLoadJsonFromFile(CONFIG_PATH);

            prefix = mayuJson.getOrCreate("prefix", new JsonPrimitive(prefix)).getAsString();
            token = mayuJson.getOrCreate("token", new JsonPrimitive(token)).getAsString();
            exceptionMessageChannelID = mayuJson.getOrCreate("exceptionMessageChannelID", new JsonPrimitive(exceptionMessageChannelID)).getAsLong();
            ownerID = mayuJson.getOrCreate("ownerID", new JsonPrimitive(ownerID)).getAsLong();
            debug = mayuJson.getOrCreate("debug", new JsonPrimitive(debug)).getAsBoolean();

            mayuJson.saveJson();

            Logger.info("Config loading done!");
            Logger.info("- Using prefix: " + prefix);
            Logger.info("- Owner ID: " + ownerID);
            Logger.info("- Exception MSG Channel: " + exceptionMessageChannelID);
            Logger.info("- Debug: " + debug);
        } catch (Exception exception) {
            exception.printStackTrace();
            Logger.error("Error occurred while loading config from path " + CONFIG_PATH + "!");
        }
    }

    public static void save() {
        try {
            MayuJson mayuJson = JsonUtil.createOrLoadJsonFromFile(CONFIG_PATH);

            mayuJson.add("prefix", prefix);
            mayuJson.add("token", token);
            mayuJson.add("exceptionMessageChannelID", exceptionMessageChannelID);
            mayuJson.add("ownerID", ownerID);
            mayuJson.add("debug", debug);

            mayuJson.saveJson();

            Logger.info("Config saving done!");
        } catch (Exception exception) {
            exception.printStackTrace();
            Logger.error("Error occurred while saving config to path " + CONFIG_PATH + "!");
        }
    }
}
