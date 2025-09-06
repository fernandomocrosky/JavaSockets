package projeto;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import projeto.handlers.StatusCode;

import javafx.scene.control.TextInputControl;

public class Validator {

    public static JsonObject validate(JsonObject request, List<String> requiredFields) {
        JsonObject response = new JsonObject();
        List<String> errors = new ArrayList<>();
        response.addProperty("status", StatusCode.BAD_REQUEST);
        response.addProperty("message", StatusCode.getMessage(StatusCode.BAD_REQUEST));

        for (String field : requiredFields) {
            if (!request.has(field)) {
                errors.add("O campo " + field + " é obrigatório.");
            }
        }

        if (!errors.isEmpty()) {
            JsonArray errorsArray = new Gson().toJsonTree(errors).getAsJsonArray();
            response.add("errors", errorsArray);
            return response;

        }

        return null;
    }

    public static List<String> validateRequest(JsonObject request, List<String> requiredFields) {
        JsonObject response = new JsonObject();
        List<String> errors = new ArrayList<>();
        response.addProperty("status", StatusCode.BAD_REQUEST);
        response.addProperty("message", StatusCode.getMessage(StatusCode.BAD_REQUEST));

        for (String field : requiredFields) {
            if (!request.has(field)) {
                errors.add("O campo " + field + " é obrigatório.");
            }
        }

        if (!errors.isEmpty()) {
            return errors;
        }

        return null;
    }

    public static List<String> validateFields(List<String> requiredFields, TextInputControl... inputs) {
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < requiredFields.size(); i++) {
            String fieldName = requiredFields.get(i);
            String value = inputs[i].getText();

            if (value == null || value.isBlank()) {
                errors.add("O campo " + fieldName + " é obrigatório.");
            }
        }

        return errors;
    }

}
