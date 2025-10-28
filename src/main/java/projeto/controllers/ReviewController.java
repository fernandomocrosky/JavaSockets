package projeto.controllers;

import com.google.gson.JsonObject;

import projeto.dao.ReviewDAO;
import projeto.handlers.JsonHandler;
import projeto.handlers.JwtHandle;
import projeto.handlers.StatusCode;
import projeto.models.Review;

public class ReviewController {
    public static String cadastrar(String request) {
        JsonObject requestJson = JsonHandler.stringToJsonObject(request);
        JsonObject responseObject = new JsonObject();
        JsonObject reviewObject = requestJson.get("review").getAsJsonObject();
        String token = requestJson.get("token").getAsString();

        String userId = JwtHandle.getClaim(token, "id", String.class);

        Review review = new Review();
        review.id_usuario = userId;
        review.id_filme = reviewObject.get("id_filme").getAsString();
        review.titulo = reviewObject.get("titulo").getAsString();
        review.descricao = reviewObject.get("descricao").getAsString();
        review.nota = reviewObject.get("nota").getAsString();

        if (ReviewDAO.insert(review)) {
            responseObject.addProperty("status", StatusCode.OK);
            responseObject.addProperty("mensagem", StatusCode.getMessage(StatusCode.OK));
        } else {
            responseObject.addProperty("status", StatusCode.BAD_REQUEST);
            responseObject.addProperty("mensagem", StatusCode.getMessage(StatusCode.BAD_REQUEST));
        }

        return JsonHandler.jsonToString(responseObject);
    }
}
