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
import projeto.models.Review;

public class FilmeController {
    public static String cadastrar(String request) {
        JsonObject response = new JsonObject();
        JsonObject requestJson = JsonHandler.stringToJsonObject(request);
        JsonObject filmeJson = requestJson.get("filme").getAsJsonObject();

        List<String> errors = Validator.validateRequest(filmeJson,
                List.of("titulo", "diretor", "ano", "sinopse", "genero"));

        if (errors != null && !errors.isEmpty()) {
            response.addProperty("status", StatusCode.METHOD_NOT_ALLOWED);
            response.addProperty("mensagem", StatusCode.getMessage(StatusCode.METHOD_NOT_ALLOWED));
            return JsonHandler.jsonToString(response);
        }

        Filme filmeDb = FilmeDAO.findFilme(filmeJson.get("titulo").getAsString().trim(),
                filmeJson.get("diretor").getAsString().trim());

        if (filmeDb.id != null) {
            response.addProperty("status", StatusCode.ALREADY_EXISTS);
            response.addProperty("mensagem", StatusCode.getMessage(StatusCode.ALREADY_EXISTS));
            return JsonHandler.jsonToString(response);
        } else {
            JsonArray generos = filmeJson.get("genero").getAsJsonArray();
            List<String> generosList = new ArrayList<>();

            for (int i = 0; i < generos.size(); i++) {
                generosList.add(generos.get(i).getAsString().trim());
            }

            Filme filme = new Filme(filmeJson.get("titulo").getAsString().trim(),
                    filmeJson.get("diretor").getAsString().trim(),
                    filmeJson.get("ano").getAsString().trim(),
                    filmeJson.get("sinopse").getAsString().trim(),
                    generosList);

            if (FilmeDAO.insert(filme)) {
                response.addProperty("status", StatusCode.CREATED);
                response.addProperty("mensagem", StatusCode.getMessage(StatusCode.CREATED));
            } else {
                response.addProperty("status", StatusCode.INTERNAL_SERVER_ERROR);
                response.addProperty("mensagem", StatusCode.getMessage(StatusCode.INTERNAL_SERVER_ERROR));
            }
        }

        return JsonHandler.jsonToString(response);
    }

    public static String listar(String request) {
        JsonObject response = new JsonObject();

        List<Filme> filmes = FilmeDAO.findAll();

        response.addProperty("status", StatusCode.OK);
        response.addProperty("mensagem", StatusCode.getMessage(StatusCode.OK));
        response.add("filmes", JsonHandler.modelToJsonArray(filmes));

        return JsonHandler.jsonToString(response);
    }

    public static String update(String request) {
        JsonObject response = new JsonObject();
        JsonObject requestJson = JsonHandler.stringToJsonObject(request);
        JsonObject filmeJson = requestJson.get("filme").getAsJsonObject();

        List<String> errors = Validator.validateRequest(filmeJson,
                List.of("titulo", "diretor", "ano", "sinopse", "genero"));

        if (errors != null && !errors.isEmpty()) {
            response.addProperty("status", StatusCode.METHOD_NOT_ALLOWED);
            response.addProperty("mensagem", StatusCode.getMessage(StatusCode.METHOD_NOT_ALLOWED));
            return JsonHandler.jsonToString(response);
        }

        Filme updateFilme = JsonHandler.jsonToModel(filmeJson, Filme.class);

        Filme filmeDb = FilmeDAO.findFilmeById(updateFilme);

        if (filmeDb.id != null) {
            if (FilmeDAO.update(updateFilme)) {
                response.addProperty("status", StatusCode.OK);
                response.addProperty("mensagem", StatusCode.getMessage(StatusCode.OK));
            } else {
                response.addProperty("status", StatusCode.INTERNAL_SERVER_ERROR);
                response.addProperty("mensagem", StatusCode.getMessage(StatusCode.INTERNAL_SERVER_ERROR));
            }
        } else {
            response.addProperty("status", StatusCode.NOT_FOUND);
            response.addProperty("mensagem", StatusCode.getMessage(StatusCode.NOT_FOUND));
            return JsonHandler.jsonToString(response);
        }

        return JsonHandler.jsonToString(response);
    }

    public static String delete(String request) {
        JsonObject response = new JsonObject();
        JsonObject requestJson = JsonHandler.stringToJsonObject(request);
        String id = requestJson.get("id").getAsString();
        if (FilmeDAO.delete(id)) {
            response.addProperty("status", StatusCode.OK);
            response.addProperty("mensagem", StatusCode.getMessage(StatusCode.OK));
        } else {
            response.addProperty("status", StatusCode.NOT_FOUND);
            response.addProperty("mensagem", StatusCode.getMessage(StatusCode.NOT_FOUND));
        }

        return JsonHandler.jsonToString(response);
    }

    public static String listarPorId(String request) {
        JsonObject response = new JsonObject();
        JsonObject requestJson = JsonHandler.stringToJsonObject(request);
        
        if (!requestJson.has("id_filme") || requestJson.get("id_filme").isJsonNull()) {
            response.addProperty("status", StatusCode.BAD_REQUEST);
            response.addProperty("mensagem", StatusCode.getMessage(StatusCode.BAD_REQUEST));
            return JsonHandler.jsonToString(response);
        }

        String id = requestJson.get("id_filme").getAsString();
        Filme filme = FilmeDAO.findById(id);

        if (filme != null && filme.id != null) {
            response.addProperty("status", StatusCode.OK);
            response.addProperty("mensagem", StatusCode.getMessage(StatusCode.OK));
            JsonObject filmeJson = JsonHandler.stringToJsonObject(JsonHandler.modelToString(filme));
            response.add("filme", filmeJson);
            
            // Buscar reviews do filme
            List<Review> reviews = FilmeDAO.findReviewsByFilmeId(id);
            response.add("reviews", JsonHandler.modelToJsonArray(reviews));
        } else {
            response.addProperty("status", StatusCode.NOT_FOUND);
            response.addProperty("mensagem", StatusCode.getMessage(StatusCode.NOT_FOUND));
        }

        return JsonHandler.jsonToString(response);
    }
}
