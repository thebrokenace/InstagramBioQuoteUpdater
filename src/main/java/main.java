import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.exceptions.IGLoginException;
import com.github.instagram4j.instagram4j.exceptions.IGResponseException;
import com.github.instagram4j.instagram4j.utils.IGChallengeUtils;
import com.google.gson.*;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.opencsv.CSVReader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class main {
    public static int characterLimit = 150;
    public static int frequencyInMinutes = 60;
    public static String username = "";
    public static String password = "";
    public static String emoji_list_path = "full-emoji-list.json";
    public static String quotes_csv_path = "quotes.csv";
    public static IGClient client;

    static {
        try {
            client = IGClient.builder().username(username).password(password).login();
        } catch (IGLoginException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws Exception {

        //System.out.println("BIO + \n" + currentBio);
        //emojiMap();
        //start Updating
        updateBioTimer();
    }
//    public static IGClient.Builder.LoginHandler twoFactorHandler = (client, response) -> {
//        // included utility to resolve two factor
//        // may specify retries. default is 3
//        return IGChallengeUtils.resolveTwoFactor(client, response, inputCode);
//    };

    public static Scanner scanner = new Scanner(System.in);

    // Callable that returns inputted code from System.in
    public static Callable<String> inputCode = () -> {
        System.out.print("Please input code: ");
        return scanner.nextLine();
    };
    public static Map<List<String>, String> emojiMap() throws IOException {
        //key is word, value is emoji
        Map<List<String>, String> emojiMap = new HashMap<>();

        Gson gson = new Gson();

        //Reader reader = new InputStreamReader(main.class.getResourceAsStream(emoji_list_path), "UTF-8");

        //String json = new String();
        byte[] bytes = main.class.getResourceAsStream(emoji_list_path).readAllBytes();
        //byte[] bytes = Files.readAllBytes(Paths.get(emoji_list_path));
        String json = new String(bytes, StandardCharsets.UTF_8);
        //String json = new String();
        //System.out.println(json);

        JsonObject sub = gson.fromJson(json, JsonObject.class);
        JsonObject object = sub.getAsJsonObject();
        JsonArray array = object.getAsJsonArray("Smileys & People");

        for (int i = 0; i < array.size(); i++) {
            String emoji = array.get(i).getAsJsonObject().getAsJsonPrimitive("emoji").toString();
            List<String> keywords = Arrays.stream(array.get(i).getAsJsonObject().getAsJsonArray("keywords").toString().replace("[", "").replace("]", "").split(",")).toList();

            emojiMap.put(keywords, emoji);
           // System.out.println(emoji);
           // System.out.println(keywords);
        }

        return emojiMap;

//        ArrayList<String> codes = new ArrayList<String>();
//        for (Map.Entry<String,JsonElement> entry : object.entrySet()) {
//            JsonArray array = entry.getValue().getAsJsonObject().getAsJsonArray("emoji");
//            for (JsonElement codeHolder : array) {
//                codes.add(codeHolder.getAsJsonObject().getAsJsonPrimitive("emoji").getAsString());
//            }
//        }

        //List<String> array = (List<String>) subs.get("Smileys & People");
       // System.out.println(codes);
//        for (String s : array) {
//            System.out.println(s + "\n");
//        }
        //Map<String, String[]> son = new Gson().fromJson(json, mapType);
        //System.out.println(array);

    }


    public static String bestEmoji (String s) throws IOException {
        Map<String, Integer> bestFit = new HashMap<>();
        for (List<String> list : emojiMap().keySet()) {
            for (String compare : list) {
                compare = compare.replace("\"", "");
                String[] words = s.split("\\s+");
                for (int i = 0; i < words.length; i++) {
                    words[i] = words[i].replaceAll("[^\\w]", "");
                    //System.out.println(words[i].toLowerCase() + " test " + compare.toLowerCase());
//                    if ((StringUtils.getLevenshteinDistance(words[i].toLowerCase(), compare.toLowerCase()) <= 3)) {
                    if (words[i].toLowerCase().equals(compare.toLowerCase())) {

                        String emoji = emojiMap().get(list);
                        bestFit.put(emoji, bestFit.getOrDefault(emoji, 0) + 1);

                    }
                }




            }
        }

        if (bestFit.isEmpty()) {
            //System.out.println("no meoji");
            List<String> defaultEmojis = new ArrayList<>();
            defaultEmojis.add(" \uD83D\uDCDA");
            defaultEmojis.add(" \uD83E\uDDD0");
            defaultEmojis.add(" ℹ️");
            defaultEmojis.add(" \uD83D\uDCA1");
            defaultEmojis.add(" \uD83D\uDCAB");
            defaultEmojis.add(" \uD83C\uDF07");
            defaultEmojis.add(" \uD83C\uDF0C");
            defaultEmojis.add(" \uD83D\uDD25");
            defaultEmojis.add(" \uD83D\uDD11");


            Random rand = new Random();
            return defaultEmojis.get(rand.nextInt(defaultEmojis.size()));
        }
        int best = 0;
        String bestEmoji = "";
        for (String se : bestFit.keySet()) {
            int value = bestFit.get(se);
            if (value > best) {
                best = value;
                bestEmoji = se;
            }
        }
        bestEmoji = bestEmoji.replace("\"", "");
        //System.out.println(s + " " + bestEmoji);
        return " " + bestEmoji;
    }

//    public static String randomEmoji() {
//        return
//    }

    public static void updateBioTimer () {
//        Exception in thread "Timer-0" java.lang.RuntimeException: java.util.concurrent.ExecutionException: com.github.instagram4j.instagram4j.exceptions.IGResponseException: login_required
//        at main$1.run(main.java:158)
//        at java.base/java.util.TimerThread.mainLoop(Timer.java:566)
//        at java.base/java.util.TimerThread.run(Timer.java:516)
//        Caused by: java.util.concurrent.ExecutionException: com.github.instagram4j.instagram4j.exceptions.IGResponseException: login_required
//        at java.base/java.util.concurrent.CompletableFuture.reportGet(CompletableFuture.java:396)
//        at java.base/java.util.concurrent.CompletableFuture.get(CompletableFuture.java:2073)
//        at main$1.run(main.java:156)
//        ... 2 more
//        Caused by: com.github.instagram4j.instagram4j.exceptions.IGResponseException: login_required
//        at com.github.instagram4j.instagram4j.requests.IGRequest.parseResponse(IGRequest.java:72)
//        at com.github.instagram4j.instagram4j.IGClient.lambda$sendRequest$4(IGClient.java:154)
//        at java.base/java.util.concurrent.CompletableFuture$UniApply.tryFire(CompletableFuture.java:646)
//        at java.base/java.util.concurrent.CompletableFuture.postComplete(CompletableFuture.java:510)
//        at java.base/java.util.concurrent.CompletableFuture.complete(CompletableFuture.java:2179)
//        at com.github.instagram4j.instagram4j.IGClient$2.onResponse(IGClient.java:141)
//        at okhttp3.internal.connection.RealCall$AsyncCall.run(RealCall.kt:504)
//        at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1144)
//        at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:642)
//        at java.base/java.lang.Thread.run(Thread.java:1589)

        Timer timer = new Timer ();
        TimerTask hourlyTask = new TimerTask() {
            @Override
            public void run () {
                String currentBio = null;
                try {
                    //error here, IG response except... but it isn't thrown
                    //caused by login_Required
                    //maybe check if client is logged in b4 updating?
                    //instagram autolock?
                    if (client.isLoggedIn()) {
                        currentBio = client.actions().account().currentUser().get().getUser().getBiography();
                    } else {
                        client = IGClient.builder().username(username).password(password).login();
                        currentBio = client.actions().account().currentUser().get().getUser().getBiography();
                    }
                } catch (InterruptedException | ExecutionException | IGLoginException e ) {
                    throw new RuntimeException(e);
                }

                List<String> bio = new ArrayList<>(Arrays.stream(currentBio.split("\\r?\\n")).toList());
                int charactersUsed = 0;
                for (int i = 0; i < bio.size(); i++) {
                    if (!(bio.size()-1 == i)) {
                        charactersUsed += bio.get(i).length();
                    }
                }
                characterLimit = 150 - charactersUsed;
                //System.out.println("charaacters used by all but last line = " + charactersUsed + "remaining characters = " + characterLimit);
                //System.out.println(bio.get(0));
                bio.set(bio.size()-1, randomQuote());
                String fixed = String.join("\n", bio);
                client.actions().account().setBio(fixed);

                System.out.println("Updated " + client.getSelfProfile().getUsername()  + " bio to \"" + fixed.replace("\n", ", ") + "\"!");

            }
        };
        timer.schedule (hourlyTask, 0l, 1000 * 60 * frequencyInMinutes);
    }



    public static String randomQuote () {
        Random rand = new Random();
        //System.out.println("random quote + " + quotes().get(rand.nextInt(quotes().size())));
        return quotes().get(rand.nextInt(quotes().size()));
    }

    public static List<String> quotes () {
        return readDataLineByLine(quotes_csv_path);
    }

    public static List<String> readDataLineByLine(String file)
    {
        //char lim is 94
        //
        List<String> bios = new ArrayList<>();

        try {

            // Create an object of filereader
            // class with CSV file as a parameter.

            InputStream input = main.class.getResourceAsStream("quotes.csv");

            File tempFile = File.createTempFile( "quotes", ".csv" );
            FileUtils.copyToFile( input, tempFile );

            FileReader filereader = new FileReader(tempFile);

            // create csvReader object passing
            // file reader as a parameter
            CSVReader csvReader = new CSVReader(filereader);

            String[] nextRecord;


            // we are going to read data line by line
            while ((nextRecord = csvReader.readNext()) != null) {
                //for (String cell : nextRecord) {

                    //System.out.print(nextRecord[1] + "\t");

                if (nextRecord[1].trim().length() + bestEmoji(nextRecord[1].trim()).length() <= characterLimit) {
                    bios.add(nextRecord[1].trim() + " " + bestEmoji(nextRecord[1].trim()));
                }
                //}
               // System.out.println();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return bios;
    }
}
