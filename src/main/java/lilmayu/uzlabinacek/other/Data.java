package lilmayu.uzlabinacek.other;

import com.google.gson.JsonObject;
import lilmayu.mayusjsonutils.JsonUtil;
import lilmayu.mayusjsonutils.objects.MayuJson;
import lilmayu.mayuslibrary.logging.Logger;

import java.io.File;

public class Data {

    public static final String DATA_PATH = "./data/";

    public static MayuJson loadDataFile(String name) {
        try {
            return JsonUtil.createOrLoadJsonFromFile(DATA_PATH + name);
        } catch (Exception exception) {
            Logger.error("Could not create data file with name " + name + "! (DATA_PATH: " + DATA_PATH + ")");
            throw new RuntimeException(exception);
        }
    }

    public static MayuJson saveDataFile(String name, JsonObject jsonObject) {
        try {
            return JsonUtil.saveJson(jsonObject, new File(DATA_PATH + name));
        } catch (Exception exception) {
            Logger.error("Could not save data file with name " + name + "! (DATA_PATH: " + DATA_PATH + ")");
            throw new RuntimeException(exception);
        }
    }

    public static MayuJson saveDataFile(MayuJson mayuJson) {
        try {
            mayuJson.saveJson();
            return mayuJson;
        } catch (Exception exception) {
            Logger.error("Could not save data file with name " + mayuJson.getFile() + "!");
            throw new RuntimeException(exception);
        }
    }
}
