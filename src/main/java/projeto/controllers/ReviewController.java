package projeto.controllers;

import java.util.List;

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
        review.id_filme = reviewObject.get("id_filme").getAsString().trim();
        review.titulo = reviewObject.get("titulo").getAsString().trim();
        review.descricao = reviewObject.get("descricao").getAsString().trim();
        review.nota = reviewObject.get("nota").getAsString().trim();

        if (ReviewDAO.insert(review)) {
            responseObject.addProperty("status", StatusCode.OK);
            responseObject.addProperty("mensagem", StatusCode.getMessage(StatusCode.OK));
        } else {
            responseObject.addProperty("status", StatusCode.BAD_REQUEST);
            responseObject.addProperty("mensagem", StatusCode.getMessage(StatusCode.BAD_REQUEST));
        }

        return JsonHandler.jsonToString(responseObject);
    }

    public static String listarPorUsuario(String request) {
        JsonObject response = new JsonObject();
        JsonObject requestJson = JsonHandler.stringToJsonObject(request);
        
        if (!requestJson.has("token") || requestJson.get("token").isJsonNull()) {
            response.addProperty("status", StatusCode.BAD_REQUEST);
            response.addProperty("mensagem", StatusCode.getMessage(StatusCode.BAD_REQUEST));
            return JsonHandler.jsonToString(response);
        }

        String token = requestJson.get("token").getAsString();
        String userId = JwtHandle.getClaim(token, "id", String.class);

        if (userId == null || userId.isEmpty()) {
            response.addProperty("status", StatusCode.UNAUTHORIZED);
            response.addProperty("mensagem", StatusCode.getMessage(StatusCode.UNAUTHORIZED));
            return JsonHandler.jsonToString(response);
        }

        List<Review> reviews = ReviewDAO.findByUsuarioId(userId);
        
        response.addProperty("status", StatusCode.OK);
        response.addProperty("mensagem", StatusCode.getMessage(StatusCode.OK));
        response.add("reviews", JsonHandler.modelToJsonArray(reviews));

        return JsonHandler.jsonToString(response);
    }

    public static String editar(String request) {
        JsonObject response = new JsonObject();
        JsonObject requestJson = JsonHandler.stringToJsonObject(request);
        
        if (!requestJson.has("token") || requestJson.get("token").isJsonNull()) {
            response.addProperty("status", StatusCode.BAD_REQUEST);
            response.addProperty("mensagem", StatusCode.getMessage(StatusCode.BAD_REQUEST));
            return JsonHandler.jsonToString(response);
        }

        if (!requestJson.has("review") || requestJson.get("review").isJsonNull()) {
            response.addProperty("status", StatusCode.BAD_REQUEST);
            response.addProperty("mensagem", StatusCode.getMessage(StatusCode.BAD_REQUEST));
            return JsonHandler.jsonToString(response);
        }

        String token = requestJson.get("token").getAsString();
        String userId = JwtHandle.getClaim(token, "id", String.class);
        String role = JwtHandle.getClaim(token, "funcao", String.class);

        if (userId == null || userId.isEmpty()) {
            response.addProperty("status", StatusCode.UNAUTHORIZED);
            response.addProperty("mensagem", StatusCode.getMessage(StatusCode.UNAUTHORIZED));
            return JsonHandler.jsonToString(response);
        }

        JsonObject reviewObject = requestJson.get("review").getAsJsonObject();
        
        if (!reviewObject.has("id") || reviewObject.get("id").isJsonNull()) {
            response.addProperty("status", StatusCode.BAD_REQUEST);
            response.addProperty("mensagem", StatusCode.getMessage(StatusCode.BAD_REQUEST));
            return JsonHandler.jsonToString(response);
        }

        String reviewId = reviewObject.get("id").getAsString();
        
        // Verificar se a review existe
        Review reviewExistente = ReviewDAO.findById(reviewId);
        if (reviewExistente == null) {
            response.addProperty("status", StatusCode.NOT_FOUND);
            response.addProperty("mensagem", StatusCode.getMessage(StatusCode.NOT_FOUND));
            return JsonHandler.jsonToString(response);
        }

        // Se não for admin, verificar se a review pertence ao usuário
        if (!"admin".equals(role) && !reviewExistente.getIdUsuario().equals(userId)) {
            response.addProperty("status", StatusCode.FORBIDDEN);
            response.addProperty("mensagem", StatusCode.getMessage(StatusCode.FORBIDDEN));
            return JsonHandler.jsonToString(response);
        }

        // Atualizar a review
        Review review = new Review();
        review.id = reviewId;
        review.titulo = reviewObject.get("titulo").getAsString().trim();
        review.descricao = reviewObject.get("descricao").getAsString().trim();
        review.nota = reviewObject.get("nota").getAsString().trim();

        if (ReviewDAO.update(review)) {
            response.addProperty("status", StatusCode.OK);
            response.addProperty("mensagem", StatusCode.getMessage(StatusCode.OK));
        } else {
            response.addProperty("status", StatusCode.INTERNAL_SERVER_ERROR);
            response.addProperty("mensagem", StatusCode.getMessage(StatusCode.INTERNAL_SERVER_ERROR));
        }

        return JsonHandler.jsonToString(response);
    }

    public static String excluir(String request) {
        JsonObject response = new JsonObject();
        JsonObject requestJson = JsonHandler.stringToJsonObject(request);
        
        if (!requestJson.has("token") || requestJson.get("token").isJsonNull()) {
            response.addProperty("status", StatusCode.BAD_REQUEST);
            response.addProperty("mensagem", StatusCode.getMessage(StatusCode.BAD_REQUEST));
            return JsonHandler.jsonToString(response);
        }

        if (!requestJson.has("id") || requestJson.get("id").isJsonNull()) {
            response.addProperty("status", StatusCode.BAD_REQUEST);
            response.addProperty("mensagem", StatusCode.getMessage(StatusCode.BAD_REQUEST));
            return JsonHandler.jsonToString(response);
        }

        String token = requestJson.get("token").getAsString();
        String userId = JwtHandle.getClaim(token, "id", String.class);
        String role = JwtHandle.getClaim(token, "funcao", String.class);

        if (userId == null || userId.isEmpty()) {
            response.addProperty("status", StatusCode.UNAUTHORIZED);
            response.addProperty("mensagem", StatusCode.getMessage(StatusCode.UNAUTHORIZED));
            return JsonHandler.jsonToString(response);
        }

        String reviewId = requestJson.get("id").getAsString();
        
        // Verificar se a review existe
        Review reviewExistente = ReviewDAO.findById(reviewId);
        if (reviewExistente == null) {
            response.addProperty("status", StatusCode.NOT_FOUND);
            response.addProperty("mensagem", StatusCode.getMessage(StatusCode.NOT_FOUND));
            return JsonHandler.jsonToString(response);
        }

        // Se não for admin, verificar se a review pertence ao usuário
        if (!"admin".equals(role) && !reviewExistente.getIdUsuario().equals(userId)) {
            response.addProperty("status", StatusCode.FORBIDDEN);
            response.addProperty("mensagem", StatusCode.getMessage(StatusCode.FORBIDDEN));
            return JsonHandler.jsonToString(response);
        }

        if (ReviewDAO.delete(reviewId)) {
            response.addProperty("status", StatusCode.OK);
            response.addProperty("mensagem", StatusCode.getMessage(StatusCode.OK));
        } else {
            response.addProperty("status", StatusCode.INTERNAL_SERVER_ERROR);
            response.addProperty("mensagem", StatusCode.getMessage(StatusCode.INTERNAL_SERVER_ERROR));
        }

        return JsonHandler.jsonToString(response);
    }
}
