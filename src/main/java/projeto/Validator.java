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

        // Verifica campos obrigatórios (inclusive aninhados com ".")
        for (String field : requiredFields) {
            String[] parts = field.split("\\.");
            JsonObject current = request;
            boolean exists = true;

            for (int i = 0; i < parts.length; i++) {
                String part = parts[i];
                if (i == parts.length - 1) {
                    if (!current.has(part)) {
                        exists = false;
                    }
                } else {
                    if (current.has(part) && current.get(part).isJsonObject()) {
                        current = current.getAsJsonObject(part);
                    } else {
                        exists = false;
                    }
                }
            }

            if (!exists) {
                errors.add("O campo '" + field + "' é obrigatório.");
            }
        }

        if (request.has("usuario")) {
            String username;

            if (request.get("usuario").isJsonObject()) {
                JsonObject usuarioObj = request.getAsJsonObject("usuario");
                if (usuarioObj.has("nome") && !usuarioObj.get("nome").isJsonNull()) {
                    username = usuarioObj.get("nome").getAsString();
                } else {
                    username = "";
                }
            } else {
                username = request.get("usuario").getAsString();
            }

            if (username.length() < 3) {
                errors.add("O campo 'usuario' deve ter pelo menos 3 caracteres.");
            }

            if (!username.matches("[a-zA-Z0-9]+")) {
                errors.add("O campo 'usuario' deve conter apenas letras e numeros.");
            }
        }

        if (request.has("senha")) {
            String password = request.get("senha").getAsString();
            if (password.length() < 3) {
                errors.add("O campo 'senha' deve ter pelo menos 3 caracteres.");
            }

            if (!password.matches("[a-zA-Z0-9]+")) {
                errors.add("O campo 'senha' deve conter apenas letras e numeros.");
            }
        }

        if (!errors.isEmpty()) {
            return errors;
        }

        return errors;
    }

    public static List<String> validateFields(List<String> requiredFields, TextInputControl... inputs) {

        List<String> errors = new ArrayList<>();

        // Verificação dos obrigatorios
        for (int i = 0; i < requiredFields.size(); i++) {
            String fieldName = requiredFields.get(i);
            String value = inputs[i].getText();

            if (value == null || value.isBlank()) {
                errors.add("O campo " + fieldName + " é obrigatório.");
            }
        }

        for (TextInputControl input : inputs) {
            String fieldName = input.getId();

            if (fieldName == null) {
                continue;
            }

            if (fieldName.equals("usuario")) {
                String username = input.getText();
                if (username.length() < 3) {
                    errors.add("O campo 'usuario' deve ter pelo menos 3 caracteres.");
                }

                if (!username.matches("[a-zA-Z0-9]+")) {
                    errors.add("O campo 'usuario' deve conter apenas letras e numeros.");
                }
            }

            if (fieldName.equals("senha")) {
                String password = input.getText();
                if (password.length() < 3) {
                    errors.add("O campo 'senha' deve ter pelo menos 3 caracteres.");
                }

                if (!password.matches("[a-zA-Z0-9]+")) {
                    errors.add("O campo 'senha' deve conter apenas letras e numeros.");
                }
            }
        }

        return errors;
    }

}
