package projeto.handlers;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JsonHandler {

    private static final Gson gson = new Gson();
    private static final Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();

    public static String prettyFormatFromString(String jsonString) {
        JsonElement jsonElement = JsonParser.parseString(jsonString);
        return gsonPretty.toJson(jsonElement);
    }

    public static String prettyFormatFromModel(Object model) {
        return gsonPretty.toJson(model);
    }

    public static String prettyFormatFromJson(JsonElement jsonElement) {
        return gsonPretty.toJson(jsonElement);
    }

    public static JsonElement stringToJson(String jsonString) {
        return JsonParser.parseString(jsonString).getAsJsonObject();
    }

    public static JsonObject stringToJsonObject(String jsonString) {
        return JsonParser.parseString(jsonString).getAsJsonObject();
    }

    public static String jsonToString(JsonElement jsonElement) {
        return gson.toJson(jsonElement);
    }

    public static <T> String modelToString(T model) {
        return gson.toJson(model);
    }

    public static <T> T stringToModel(String jsonString, Class<T> model) {
        return gson.fromJson(jsonString, model);
    }

    public static <T> void addArray(JsonObject json, String key, List<T> array) {
        JsonArray arrayJson = new JsonArray();
        for (T item : array) {
            arrayJson.add((String) item);
        }
        json.add(key, arrayJson);
    }

}
