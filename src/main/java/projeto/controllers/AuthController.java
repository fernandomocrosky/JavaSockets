package projeto.controllers;

import com.google.gson.JsonObject;

import projeto.dao.UserDAO;
import projeto.handlers.JsonHandler;
import projeto.handlers.JwtHandle;
import projeto.handlers.StatusCode;
import projeto.models.User;

public class AuthController {
    public static String login(String request) {

        JsonObject response = new JsonObject();

        try {
            JsonObject json = JsonHandler.stringToJsonObject(request);
            String usuario = json.get("usuario").getAsString();
            String senha = json.get("senha").getAsString();

            User user = UserDAO.findByUsernameAndPassword(usuario, senha);

            if (user != null) {
                if (user.getUsuario().equals("admin")) {
                    user.setRole("admin");
                } else {
                    user.setRole("user");
                }
                String token = JwtHandle.generateToken(user);
                response.addProperty("status", StatusCode.OK);
                response.addProperty("token", token);

                return JsonHandler.jsonToString(response);
            } else {
                response.addProperty("status", StatusCode.UNAUTHORIZED);
            }

        } catch (Exception e) {
            response.addProperty("status", StatusCode.INTERNAL_SERVER_ERROR);
        }

        JsonObject json = JsonHandler.stringToJsonObject(request);
        User user = new User(json.get("usuario").getAsString(), json.get("senha").getAsString());

        if (user.getUsuario().equals("admin") && user.getSenha().equals("admin")) {
            response.addProperty("status", StatusCode.OK);
        } else {
            response.addProperty("status", StatusCode.UNAUTHORIZED);
        }

        return JsonHandler.jsonToString(response);
    }

    public static String logout(String request) {
        JsonObject json = new JsonObject();
        JsonObject requestJsonObject = JsonHandler.stringToJsonObject(request);

        String token = requestJsonObject.get("token").getAsString();

        try {
            Long expMilli = JwtHandle.getExpiration(token).getTime();
            UserDAO.addTokenToBlacklist(token, expMilli);
            json.addProperty("status", StatusCode.OK);
        } catch (Exception ex) {
            json.addProperty("status", StatusCode.UNAUTHORIZED);
            System.out.println(ex.getMessage());
        }

        return JsonHandler.jsonToString(json);
    }
}
