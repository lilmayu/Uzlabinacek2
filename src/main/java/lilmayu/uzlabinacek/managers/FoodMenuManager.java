package lilmayu.uzlabinacek.managers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import lilmayu.mayusjdautilities.managed.ManagedMessage;
import lilmayu.mayusjdautilities.utils.DiscordUtils;
import lilmayu.mayusjsonutils.objects.MayuJson;
import lilmayu.mayuslibrary.logging.LogPrefix;
import lilmayu.mayuslibrary.logging.Logger;
import lilmayu.mayuslibrary.utils.StringUtils;
import lilmayu.uzlabinacek.Main;
import lilmayu.uzlabinacek.other.Data;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class FoodMenuManager {

    private final static @Getter List<ManagedMessage> managedMessages = new ArrayList<>();
    private static @Getter final Timer updateTimer = new Timer();
    private static @Getter EmbedBuilder lastSuccessfulEmbedCache = DiscordUtils.getDefaultEmbed();
    private static @Getter FoodMenu foodMenu;

    public static void init() {
        Logger.addLogPrefix(new LogPrefix("[FoodMenu]", FoodMenuManager.class));
    }

    // Managed Message stuff

    public static ManagedMessage createFoodMenuMessage(TextChannel textChannel) {
        if (isManagedMessageInTextChannel(textChannel)) {
            return null;
        }

        ManagedMessage managedMessage = new ManagedMessage("food_menu_" + System.currentTimeMillis(), textChannel.getGuild(), textChannel);
        managedMessage.updateEntries(Main.getJda());
        updateManagedMessage(managedMessage);
        managedMessages.add(managedMessage);
        save();
        return managedMessage;
    }

    public static boolean removeFoodMenuMessage(TextChannel textChannel) {
        if (!isManagedMessageInTextChannel(textChannel)) {
            return false;
        }

        ManagedMessage managedMessage = getManagedMessageByTextChannel(textChannel);

        try {
            managedMessage.getMessage().delete().queue();
        } catch (Exception exception) {
            exception.printStackTrace();
            Logger.error("Error occurred while removing Managed Message " + managedMessage.getName() + "!");
            return false;
        }

        return managedMessages.remove(managedMessage);
    }

    public static boolean updateManagedMessage(ManagedMessage managedMessage) {
        managedMessage.updateEntries(Main.getJda());
        return managedMessage.sendOrEditMessage(new MessageBuilder().setEmbeds(lastSuccessfulEmbedCache.build()));
    }

    public static boolean updateManagedMessage(TextChannel textChannel) {
        if (!isManagedMessageInTextChannel(textChannel)) {
            return false;
        }

        updateEmbedCache();
        return updateManagedMessage(getManagedMessageByTextChannel(textChannel));
    }

    public static void moveDownManagedMessage(TextChannel textChannel) {
        removeFoodMenuMessage(textChannel);
        createFoodMenuMessage(textChannel);
    }

    public static boolean isManagedMessageInTextChannel(TextChannel textChannel) {
        return getManagedMessageByTextChannel(textChannel) != null;
    }

    public static ManagedMessage getManagedMessageByTextChannel(TextChannel textChannel) {
        for (ManagedMessage managedMessage : new ArrayList<>(managedMessages)) {
            if (managedMessage.getMessageChannel().getIdLong() == textChannel.getIdLong()) {
                return managedMessage;
            }
        }

        return null;
    }

    // Core stuff

    public static void updateEmbedCache() {
        FoodMenuReceiver.updateFoodMenu();
        lastSuccessfulEmbedCache = DiscordUtils.getDefaultEmbed();
        lastSuccessfulEmbedCache.setTitle("Jídelníček");
        lastSuccessfulEmbedCache.setDescription("Jídelníček se aktualizuje každou hodinu.  Pokud by přetrvávala nějaká chyba, prosím použij příkaz `/about` a napiš vývojáři.\n" + "Poslední úspěšná aktualizace: **" + getLastSuccessfulUpdateTime() + "**");


        if (foodMenu == null) {
            lastSuccessfulEmbedCache.addField("Chyba", "Nebylo možné získat žádné data z `strava.cz`.", false);
            return;
        }

        foodMenu.cut();
        foodMenu.reverseList();
        for (Meal meal : foodMenu.getMeals()) {
            String soupFirst = meal.parseFirstSoup();
            String mainCourseFirst = meal.parseFirstMainCourse();
            String descriptionFirst = meal.parseFirstDescription();

            String soupSecond = null;
            String mainCourseSecond = null;
            String descriptionSecond = null;

            if (meal.hasSecond()) {
                soupSecond = meal.parseSecondSoup();
                mainCourseSecond = meal.parseSecondMainCourse();
                descriptionSecond = meal.parseSecondDescription();
            }

            String date = meal.parseDate();

            if (lastSuccessfulEmbedCache.getFields().size() < 25) {
                String fieldValue;
                if (!meal.hasSecond()) {
                    fieldValue = "Polévka: " + soupFirst + "\nHlavní chod: **" + mainCourseFirst + "**";

                    if (meal.hasFirstDescription()) {
                        fieldValue += "\nPopis: " + descriptionFirst;
                    }

                    lastSuccessfulEmbedCache.addField("> " + date, fieldValue, false);
                } else {
                    fieldValue = "> První oběd\n";
                    fieldValue += "Polévka: " + soupFirst + "\nHlavní chod: **" + mainCourseFirst + "**\n";
                    if (meal.hasFirstDescription()) {
                        fieldValue += "\nPopis: " + descriptionFirst + "\n";
                    }

                    fieldValue += "> Druhý oběd\n";
                    fieldValue += "Polévka: " + soupSecond + "\nHlavní chod: **" + mainCourseSecond + "**\n";
                    if (meal.hasSecondDescription()) {
                        fieldValue += "\nPopis: " + descriptionSecond;
                    }

                    lastSuccessfulEmbedCache.addField("> " + date, fieldValue, false);
                }
            }
        }
    }

    public static void startUpdateTimer() {
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Logger.info("Updating Food Menus...");
                updateEmbedCache();

                for (ManagedMessage managedMessage : new ArrayList<>(managedMessages)) {
                    updateManagedMessage(managedMessage);
                }

                Logger.info("Food Menus update completed!");
            }
        }, 1000, 3600000);
    }

    // Data stuff

    public static void load() {
        MayuJson mayuJson = Data.loadDataFile("foodmenu-data.json");
        managedMessages.clear();

        JsonArray managedMessagesJsonArray = mayuJson.getOrCreate("managedMessages", new JsonArray()).getAsJsonArray();
        for (JsonElement jsonElement : managedMessagesJsonArray) {
            ManagedMessage managedMessage = new ManagedMessage(jsonElement.getAsJsonObject());
            if (!managedMessage.updateEntries(Main.getJda(), true)) {
                Logger.warning("Error occurred while updating Managed Message! Probably incorrect data?");
            } else {
                managedMessages.add(managedMessage);
            }
        }

        Logger.info("Loaded " + managedMessages.size() + " Managed Messages!");

        Data.saveDataFile(mayuJson);
    }

    public static void save() {
        MayuJson mayuJson = Data.loadDataFile("foodmenu-data.json");

        JsonArray managedMessagesJsonArray = new JsonArray();
        for (ManagedMessage managedMessage : managedMessages) {
            managedMessagesJsonArray.add(managedMessage.toJsonObject());
        }

        mayuJson.add("managedMessages", managedMessagesJsonArray);

        Data.saveDataFile(mayuJson);
    }

    // Others

    private static String getLastSuccessfulUpdateTime() {
        if (foodMenu == null) {
            return "Nikdy";
        } else {
            return new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date(foodMenu.time));
        }
    }

    // Objects

    private static class FoodMenu {

        private @Getter List<Meal> meals;
        private @Getter long time;

        public FoodMenu(List<Meal> meals, long time) {
            this.meals = meals;
            this.time = time;
        }

        public void reverseList() {
            Collections.reverse(meals);
        }

        public void cut() {
            if (meals.size() > 25) {
                meals = meals.subList(0, 25);
            }
        }
    }

    private static class Meal {

        private @Getter String nameFirst;
        private @Getter String descriptionFirst;
        private @Getter String nameSecond;
        private @Getter String descriptionSecond;

        private @Getter String date;

        public Meal(String nameFirst, String descriptionFirst, String date) {
            this.nameFirst = nameFirst;
            if (descriptionFirst == null)
                descriptionFirst = "";
            this.descriptionFirst = descriptionFirst;
            this.date = date;
        }

        public Meal(String nameFirst, String descriptionFirst, String nameSecond, String descriptionSecond, String date) {
            this.nameFirst = nameFirst;
            if (descriptionFirst == null)
                this.descriptionFirst = "";

            this.nameSecond = nameSecond;
            if (descriptionSecond == null)
                this.descriptionSecond = "";

            this.date = date;
        }

        public String parseDate() {
            Date parsedDate;

            try {
                parsedDate = new SimpleDateFormat("yyyy-MM-dd").parse(date);
            } catch (ParseException exception) {
                exception.printStackTrace();
                Logger.warning("Could not parse date " + date + "!");
                return date;
            }

            return new SimpleDateFormat("dd.MM.yyyy", Locale.forLanguageTag("cs-CZ")).format(parsedDate) + " (" + StringUtils.prettyString(new SimpleDateFormat("EEEE",
                    Locale.forLanguageTag("cs-CZ")).format(parsedDate)) + ")";
        }

        public String parseFirstSoup() {
            int index = nameFirst.indexOf("1.");

            if (index == -1) {
                return nameFirst;
            } else {
                return nameFirst.substring(0, index);
            }
        }

        public String parseSecondSoup() {
            int index = nameSecond.indexOf("2.");

            if (index == -1) {
                return nameSecond;
            } else {
                return nameSecond.substring(0, index);
            }
        }

        public String parseFirstMainCourse() {
            int index = nameFirst.indexOf("1.");

            if (index == -1) {
                return nameFirst;
            } else {
                return nameFirst.substring(index + 2);
            }
        }

        public String parseSecondMainCourse() {
            int index = nameSecond.indexOf("2.");

            if (index == -1) {
                return nameSecond;
            } else {
                return nameSecond.substring(index + 2);
            }
        }

        public boolean hasFirstDescription() {
            return !descriptionFirst.isBlank();
        }

        public boolean hasSecondDescription() {
            return !descriptionSecond.isBlank();
        }

        public boolean hasSecond() {
            return nameSecond != null;
        }

        public String parseFirstDescription() {
            return descriptionFirst.replace("(", "").replace(")", "");
        }

        public String parseSecondDescription() {
            return descriptionSecond.replace("(", "").replace(")", "");
        }
    }

    // XML stuff

    private static class FoodMenuReceiver {

        public static void updateFoodMenu() {
            List<Meal> meals = new ArrayList<>();

            String foodMenuXMLData = getFoodMenuXMLData();
            Document foodMenuDocument;

            try {
                if (foodMenuXMLData == null) {
                    Logger.error("FoodMenuXMLData is unable to process!");
                    return;
                }

                DocumentBuilderFactory XMLFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder XMLBuilder;
                XMLBuilder = XMLFactory.newDocumentBuilder();
                foodMenuDocument = XMLBuilder.parse(new InputSource(new StringReader(foodMenuXMLData)));
            } catch (Exception exception) {
                exception.printStackTrace();
                Logger.error("Error occurred while parsing FoodMenuXMLData!");
                return;
            }

            NodeList nodeList = foodMenuDocument.getElementsByTagName("pomjidelnic_xmljidelnic");

            for (int x = 0; x < nodeList.getLength(); x++) {
                try {
                    Node node = nodeList.item(x);
                    Element element = (Element) node;

                    String name = element.getElementsByTagName("nazev").item(0).getTextContent();
                    String description = element.getElementsByTagName("popis").item(0).getTextContent();
                    String date = element.getElementsByTagName("datum").item(0).getTextContent();
                    String type = element.getElementsByTagName("druh").item(0).getTextContent();

                    if (type.equals("2")) {
                        updateMeal(meals, new Meal(name, description, date), date);
                    } else {
                        meals.add(new Meal(name, description, date));
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                    Logger.error("Error occurred while processing data from FoodMenuXML on index " + x + "!");
                }
            }

            if (!meals.isEmpty()) {
                Logger.info("Loaded " + meals.size() + " meals from FoodMenuXML.");
                foodMenu = new FoodMenu(meals, System.currentTimeMillis());
            } else {
                Logger.warning("No meals were loaded from FoodMenuXML!");
            }
        }

        public static String getFoodMenuXMLData() {
            try {
                String url = "www.strava.cz";
                Socket socket = new Socket(url, 80);

                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "windows-1250"));
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "windows-1250"));

                String msg = "GET /foxisapi/foxisapi.dll/istravne.istravne.process?xmljidelnickyA&zarizeni=3148 HTTP/1.1\r\nUser-Agent: RawHttpGet\r\nHost: www.strava.cz\r\nAccept: */*\r\n";
                sendMessage(out, msg);
                String XML = readResponse(in);

                out.close();
                in.close();

                return XML;
            } catch (Exception exception) {
                exception.printStackTrace();
                Logger.error("Error occurred while downloading data from strava.cz!");
                return null;
            }
        }

        private static void sendMessage(BufferedWriter out, String request) throws IOException {
            for (String line : getContents(request)) {
                out.write(line + "\n");
            }

            out.write("\n");
            out.flush();
        }

        private static String readResponse(BufferedReader in) throws IOException {
            String fullText = "";
            String line;
            while ((line = in.readLine()) != null) {
                fullText += line;
            }

            return fullText;
        }

        private static List<String> getContents(String file) throws IOException {
            List<String> contents = new ArrayList<>();

            BufferedReader input = new BufferedReader(new StringReader(file));
            String line;
            while ((line = input.readLine()) != null) {
                contents.add(line);
            }
            input.close();

            return contents;
        }

        private static void updateMeal(List<Meal> meals, Meal meal, String date) {
            for (Meal mealToUpdate : meals) {
                if (mealToUpdate.getDate().equalsIgnoreCase(date)) {
                    mealToUpdate.nameSecond = meal.nameFirst;
                    mealToUpdate.descriptionSecond = meal.descriptionFirst;
                }
            }
        }
    }
}
