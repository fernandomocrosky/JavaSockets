package projeto.controllers;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.jsonwebtoken.Jwt;
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

        List<String> errors = Validator.validateRequest(requestJson, List.of("usuario.nome", "usuario.senha"));

        if (errors != null && !errors.isEmpty()) {
            response.addProperty("status", StatusCode.BAD_REQUEST);
            response.addProperty("message", String.join("\n", errors));
            return JsonHandler.jsonToString(response);
        }
        JsonObject userObject = requestJson.get("usuario").getAsJsonObject();

        User user = new User(userObject.get("nome").getAsString(), userObject.get("senha").getAsString());
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

    public static String listarMeuUsuario(String request) {
        JsonObject response = new JsonObject();
        JsonObject requestJson = JsonHandler.stringToJsonObject(request);
        String token = requestJson.get("token").getAsString();

        User user = UserDAO.findById(JwtHandle.getClaim(token, "id", String.class));
        response.addProperty("usuario", user.getUsuario());
        response.addProperty("status", StatusCode.OK);
        response.addProperty("message", StatusCode.getMessage(StatusCode.OK));

        return JsonHandler.jsonToString(response);
    }

    public static String editarAdmin(String request) {
        JsonObject response = new JsonObject();
        JsonObject requestJson = JsonHandler.stringToJsonObject(request);
        List<String> errors = null;

        errors = Validator.validateRequest(requestJson, List.of("id", "usuario.senha"));

        if (errors != null && !errors.isEmpty()) {
            response.addProperty("status", StatusCode.BAD_REQUEST);
            response.addProperty("message", String.join("\n", errors));
            return JsonHandler.jsonToString(response);
        }

        User user = new User();
        user.setSenha(requestJson.get("usuario").getAsJsonObject().get("senha").getAsString());
        user.setId(requestJson.get("id").getAsString());

        if (UserDAO.update(user)) {
            response.addProperty("status", StatusCode.OK);
            response.addProperty("message", StatusCode.getMessage(StatusCode.OK));
        } else {
            response.addProperty("status", StatusCode.NOT_FOUND);
            response.addProperty("message", StatusCode.getMessage(StatusCode.NOT_FOUND));
        }

        return JsonHandler.jsonToString(response);
    }

    public static String editar(String request) {
        JsonObject response = new JsonObject();
        JsonObject requestJson = JsonHandler.stringToJsonObject(request);
        String token = requestJson.get("token").getAsString();
        List<String> errors = null;

        errors = Validator.validateRequest(requestJson, List.of("usuario.senha"));

        if (errors != null && !errors.isEmpty()) {
            response.addProperty("status", StatusCode.BAD_REQUEST);
            response.addProperty("message", String.join("\n", errors));
            return JsonHandler.jsonToString(response);
        }

        User user = new User();
        user.setSenha(requestJson.get("usuario").getAsJsonObject().get("senha").getAsString());
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
        String token = requestJson.get("token").getAsString();
        String id = JwtHandle.getClaim(token, "id", String.class);

        if (UserDAO.delete(id)) {
            response.addProperty("status", StatusCode.OK);
            response.addProperty("message", StatusCode.getMessage(StatusCode.OK));
        } else {
            response.addProperty("status", StatusCode.NOT_FOUND);
            response.addProperty("message", StatusCode.getMessage(StatusCode.NOT_FOUND));
        }

        return JsonHandler.jsonToString(response);
    }

    public static String deletarAdmin(String request) {
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
