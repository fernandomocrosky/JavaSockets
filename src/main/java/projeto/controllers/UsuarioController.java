package projeto.controllers;

import java.util.List;

import com.google.gson.JsonObject;

import projeto.Validator;
import projeto.dao.UserDAO;
import projeto.handlers.JsonHandler;
import projeto.handlers.StatusCode;
import projeto.models.User;

public class UsuarioController {
    public static String cadastrar(String request) {

        JsonObject response = new JsonObject();
        JsonObject requestJson = JsonHandler.stringToJsonObject(request);

        List<String> errors = Validator.validateRequest(requestJson, List.of("usuario", "senha"));

        if (errors != null && !errors.isEmpty()) {
            response.addProperty("status", StatusCode.BAD_REQUEST);
            response.addProperty("message", String.join("\n", errors));
            return JsonHandler.jsonToString(response);
        }

        User user = new User(requestJson.get("usuario").getAsString(), requestJson.get("senha").getAsString());
        User userDb = UserDAO.findByUsername(user.getUsuario());

        if (userDb != null) {
            response.addProperty("status", StatusCode.ALREADY_EXISTS);
            response.addProperty("message", StatusCode.getMessage(StatusCode.ALREADY_EXISTS));
            return JsonHandler.jsonToString(response);
        }

        if (UserDAO.insert(user)) {
            response.addProperty("status", StatusCode.CREATED);
            response.addProperty("message", StatusCode.getMessage(StatusCode.CREATED));
            return JsonHandler.jsonToString(response);
        }

        return JsonHandler.jsonToString(null);
    }
}
