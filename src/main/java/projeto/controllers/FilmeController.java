package projeto.controllers;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import projeto.Validator;
import projeto.dao.FilmeDAO;
import projeto.handlers.JsonHandler;
import projeto.handlers.StatusCode;
import projeto.models.Filme;

public class FilmeController {
    public static String cadastrar(String request) {
        JsonObject response = new JsonObject();
        JsonObject requestJson = JsonHandler.stringToJsonObject(request);
        JsonObject filmeJson = requestJson.get("filme").getAsJsonObject();

        List<String> errors = Validator.validateRequest(filmeJson,
                List.of("titulo", "diretor", "ano", "sinopse", "generos"));

        if (errors != null && !errors.isEmpty()) {
            response.addProperty("status", StatusCode.BAD_REQUEST);
            response.addProperty("message", String.join("\n", errors));
            return JsonHandler.jsonToString(response);
        }

        Filme filmeDb = FilmeDAO.findFilme(filmeJson.get("titulo").getAsString(),
                filmeJson.get("diretor").getAsString());

        System.out.println(filmeDb);

        if (filmeDb.id != null) {
            response.addProperty("status", StatusCode.ALREADY_EXISTS);
            response.addProperty("message", StatusCode.getMessage(StatusCode.ALREADY_EXISTS));
            return JsonHandler.jsonToString(response);
        } else {
            JsonArray generos = filmeJson.get("generos").getAsJsonArray();
            List<String> generosList = new ArrayList<>();

            for (int i = 0; i < generos.size(); i++) {
                generosList.add(generos.get(i).getAsString());
            }

            Filme filme = new Filme(filmeJson.get("titulo").getAsString(),
                    filmeJson.get("diretor").getAsString(),
                    filmeJson.get("ano").getAsString(),
                    filmeJson.get("sinopse").getAsString(),
                    generosList);

            if (FilmeDAO.insert(filme)) {
                response.addProperty("status", StatusCode.OK);
                response.addProperty("message", StatusCode.getMessage(StatusCode.OK));
            } else {
                response.addProperty("status", StatusCode.INTERNAL_SERVER_ERROR);
                response.addProperty("message", StatusCode.getMessage(StatusCode.INTERNAL_SERVER_ERROR));
            }
        }

        return JsonHandler.jsonToString(response);
    }
}
