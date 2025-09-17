package projeto.controllers;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import projeto.Validator;
import projeto.dao.UserDAO;
import projeto.handlers.JsonHandler;
import projeto.handlers.JwtHandle;
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

    public static String listar(String request) {
        JsonObject response = new JsonObject();

        List<User> users = UserDAO.findAll();
        JsonArray usersJson = new JsonArray();
        for (User user : users) {
            JsonObject userJson = new JsonObject();
            userJson.addProperty("id", user.getId());
            userJson.addProperty("usuario", user.getUsuario());
            usersJson.add(userJson);
        }
        response.addProperty("status", StatusCode.OK);
        response.addProperty("message", StatusCode.getMessage(StatusCode.OK));
        response.add("usuarios", usersJson);

        return JsonHandler.jsonToString(response);
    }

    public static String editar(String request) {
        JsonObject response = new JsonObject();
        JsonObject requestJson = JsonHandler.stringToJsonObject(request);
        String token = requestJson.get("token").getAsString();
        String role = JwtHandle.getClaim(token, "funcao", String.class);
        List<String> errors = null;
        if (role.equals("admin"))
            errors = Validator.validateRequest(requestJson, List.of("usuario", "usuario.nome", "usuario.id"));
        else
            errors = Validator.validateRequest(requestJson, List.of("usuario", "usuario.nome"));

        if (errors != null && !errors.isEmpty()) {
            response.addProperty("status", StatusCode.BAD_REQUEST);
            response.addProperty("message", String.join("\n", errors));
            return JsonHandler.jsonToString(response);
        }

        User user = new User();
        user.setUsuario(requestJson.get("usuario").getAsJsonObject().get("nome").getAsString());
        if (requestJson.get("usuario").getAsJsonObject().get("id") != null)
            user.setId(requestJson.get("usuario").getAsJsonObject().get("id").getAsString());
        else
            user.setId(JwtHandle.getClaim(token, "id", String.class));

        if (UserDAO.update(user)) {
            response.addProperty("status", StatusCode.OK);
            response.addProperty("message", StatusCode.getMessage(StatusCode.OK));
        } else {
            response.addProperty("status", StatusCode.NOT_FOUND);
            response.addProperty("message", StatusCode.getMessage(StatusCode.NOT_FOUND));
        }

        return JsonHandler.jsonToString(response);
    }

    public static String deletar(String request) {
        JsonObject response = new JsonObject();
        JsonObject requestJson = JsonHandler.stringToJsonObject(request);

        List<String> errors = Validator.validateRequest(requestJson, List.of("id"));

        if (errors != null && !errors.isEmpty()) {
            response.addProperty("status", StatusCode.BAD_REQUEST);
            response.addProperty("message", String.join("\n", errors));
            return JsonHandler.jsonToString(response);
        }

        if (UserDAO.delete(requestJson.get("id").getAsString())) {
            response.addProperty("status", StatusCode.OK);
            response.addProperty("message", StatusCode.getMessage(StatusCode.OK));
        } else {
            response.addProperty("status", StatusCode.NOT_FOUND);
            response.addProperty("message", StatusCode.getMessage(StatusCode.NOT_FOUND));
        }

        return JsonHandler.jsonToString(response);
    }
}
