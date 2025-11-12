package projeto;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import projeto.handlers.StatusCode;

import javafx.scene.control.TextInputControl;

public class Validator {

    public static JsonObject validate(JsonObject request, List<String> requiredFields) {
        JsonObject response = new JsonObject();
        List<String> errors = new ArrayList<>();
        response.addProperty("status", StatusCode.BAD_REQUEST);
        response.addProperty("mensagem", StatusCode.getMessage(StatusCode.BAD_REQUEST));

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
        List<String> errors = new ArrayList<>();

        for (String field : requiredFields) {
            String[] parts = field.split("\\.");
            JsonObject current = request;
            boolean exists = true;

            for (int i = 0; i < parts.length; i++) {
                String part = parts[i];
                if (i == parts.length - 1) {
                    if (!current.has(part) || current.get(part).isJsonNull()) {
                        exists = false;
                    } else {
                        if(field.equals("id")) break;

                        // Verifica se é array
                        if (current.get(part).isJsonArray()) {
                            JsonArray arr = current.getAsJsonArray(part);
                            if (arr.size() == 0) {
                                errors.add("O campo '" + field + "' não pode ser vazio.");
                            } else {
                                for (JsonElement el : arr) {
                                    if (!el.isJsonPrimitive()) {
                                        errors.add("O campo '" + field + "' deve conter apenas valores simples.");
                                    } else {
                                        String value = el.getAsString();
                                        if (value.length() < 3) {
                                            errors.add(
                                                    "Cada item em '" + field + "' deve ter pelo menos 3 caracteres.");
                                        }
                                        if (!value.matches("[a-zA-Z0-9À-ÿ ]+")) {
                                            errors.add("Cada item em '" + field
                                                    + "' deve conter apenas letras, números e espaços.");
                                        }
                                    }
                                }
                            }
                        } else {
                            // Validação simples para string
                            String value = current.get(part).getAsString();
                            if (value.length() < 3) {
                                errors.add("O campo '" + field + "' deve ter pelo menos 3 caracteres.");
                            }
                            if (!value.matches("[a-zA-Z0-9À-ÿ ]+")) {
                                errors.add("O campo '" + field + "' deve conter apenas letras, números e espaços.");
                            }
                        }
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
