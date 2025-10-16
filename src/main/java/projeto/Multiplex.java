package projeto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import projeto.controllers.AuthController;
import projeto.controllers.FilmeController;
import projeto.controllers.ReviewController;
import projeto.controllers.UsuarioController;
import projeto.handlers.JsonHandler;
import projeto.handlers.JwtHandle;
import projeto.handlers.StatusCode;

public class Multiplex {
    private static final Map<String, Function<String, String>> operations = new HashMap<>();
    private static final Map<String, List<String>> permissions = new HashMap<>();

    static {
        operations.put("LOGIN", AuthController::login);
        operations.put("LOGOUT", AuthController::logout);
        operations.put("CRIAR_USUARIO", UsuarioController::cadastrar);
        operations.put("LISTAR_PROPRIO_USUARIO", UsuarioController::listarMeuUsuario);
        operations.put("LISTAR_USUARIOS", UsuarioController::listar);
        operations.put("ADMIN_EDITAR_USUARIO", UsuarioController::editarAdmin);
        operations.put("EDITAR_PROPRIO_USUARIO", UsuarioController::editar);
        operations.put("EXCLUIR_PROPRIO_USUARIO", UsuarioController::deletar);
        operations.put("ADMIN_EXCLUIR_USUARIO", UsuarioController::deletarAdmin);
        operations.put("CRIAR_FILME", FilmeController::cadastrar);
        operations.put("LISTAR_FILMES", FilmeController::listar);
        operations.put("EDITAR_FILME", FilmeController::update);
        operations.put("EXCLUIR_FILME", FilmeController::delete);
        operations.put("CRIAR_REVIEW", ReviewController::cadastrar);

        permissions.put("public", List.of("LOGIN", "CRIAR_USUARIO", "LISTAR_FILMES"));

        permissions.put("admin",
                List.of("LISTAR_USUARIOS", "LOGOUT", "ADMIN_EDITAR_USUARIO", "LISTAR_PROPRIO_USUARIO", "EDITAR_PROPRIO_USUARIO",
                        "ADMIN_EXCLUIR_USUARIO", "EXCLUIR_PROPRIO_USUARIO", "CRIAR_FILME",
                        "EDITAR_FILME", "EXCLUIR_FILME"));

        permissions.put("user", List.of("LOGOUT", "CRIAR_REVIEW", "EDITAR_PROPRIO_USUARIO", "LISTAR_PROPRIO_USUARIO", "EXCLUIR_PROPRIO_USUARIO"));
    }

    public static JsonElement handle(String request) {
        if (request == null || request.isBlank()) {
            JsonObject response = new JsonObject();
            response.addProperty("status", StatusCode.BAD_REQUEST);
            response.addProperty("message", String.format("Erro : %s possivel causa %s",
                    StatusCode.getMessage(StatusCode.BAD_REQUEST), "Mensagem vazia"));
            return response;
        }

        JsonObject requestObject = JsonHandler.stringToJsonObject(request);
        JsonObject responseObject = new JsonObject();

        if (!requestObject.has("operacao") ||
                requestObject.get("operacao").isJsonNull() ||
                requestObject.get("operacao").getAsString().isBlank()) {
            responseObject.addProperty("status", StatusCode.BAD_REQUEST);
            responseObject.addProperty("message", String.format("Erro : %s possivel causa %s",
                    StatusCode.getMessage(StatusCode.BAD_REQUEST), "Falta a chave operacao na mensagem"));
            return responseObject;
        }

        String operation = JsonHandler.stringToJsonObject(request).get("operacao").getAsString();

        Function<String, String> handler = operations.get(operation);

        if (handler == null) {

            if (!requestObject.has("operacao") ||
                    requestObject.get("operacao").isJsonNull() ||
                    requestObject.get("operacao").getAsString().isBlank()) {
                responseObject.addProperty("status", StatusCode.BAD_REQUEST);
                responseObject.addProperty("message", String.format("Erro : %s possivel causa %s",
                        StatusCode.getMessage(StatusCode.BAD_REQUEST), "Falta a chave operacao na mensagem"));
                return responseObject;
            }

            responseObject.addProperty("status", StatusCode.INTERNAL_SERVER_ERROR);
            responseObject.addProperty("message", "Operacao nao encontrada no servidor.");
            return responseObject;
        }

        if (!permissions.get("public").contains(operation)) {

            String token = requestObject.get("token").getAsString();
            String role = JwtHandle.getClaim(token, "funcao", String.class);

            if (!requestObject.has("token")) {
                responseObject.addProperty("status", StatusCode.UNAUTHORIZED);
                responseObject.addProperty("message", StatusCode.getMessage(StatusCode.UNAUTHORIZED));
                return responseObject;
            }

            if (role == null || token == null) {
                responseObject.addProperty("status", StatusCode.UNAUTHORIZED);
                responseObject.addProperty("message", "Falta token ou funcao no token");
                return responseObject;
            }

            if (!permissions.get(role).contains(operation)) {
                responseObject.addProperty("status", StatusCode.FORBIDDEN);
                responseObject.addProperty("message", StatusCode.getMessage(StatusCode.FORBIDDEN));
                return responseObject;
            }
        }

        String response = operations.get(operation).apply(request);
        JsonElement jsonResponse = JsonHandler.stringToJson(response);

        if (response == null || response.isEmpty()) {
            responseObject.addProperty("status", StatusCode.INTERNAL_SERVER_ERROR);
            responseObject.addProperty("message", StatusCode.getMessage(StatusCode.INTERNAL_SERVER_ERROR));
        }

        return jsonResponse;
    }
}
