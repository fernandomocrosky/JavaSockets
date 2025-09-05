package projeto.controllers;

import com.google.gson.JsonObject;

import projeto.handlers.JsonHandler;
import projeto.handlers.StatusCode;
import projeto.models.User;

public class LoginController {
    public static String login(String request) {
        JsonObject json = JsonHandler.stringToJsonObject(request);
        User user = new User(json.get("usuario").getAsString(), json.get("senha").getAsString(), "");

        JsonObject response = new JsonObject();

        if (user.getUsuario().equals("admin") && user.getSenha().equals("admin")) {
            response.addProperty("status", StatusCode.OK);
            response.addProperty("status", StatusCode.getMessage(StatusCode.OK));
        } else {
            response.addProperty("status", StatusCode.UNAUTHORIZED);
            response.addProperty("message", StatusCode.getMessage(StatusCode.UNAUTHORIZED));
        }

        return JsonHandler.jsonToString(response);
    }
}
