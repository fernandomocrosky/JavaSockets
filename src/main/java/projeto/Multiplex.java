package projeto;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import projeto.controllers.AuthController;
import projeto.controllers.UsuarioController;
import projeto.handlers.JsonHandler;
import projeto.handlers.StatusCode;

public class Multiplex {
    private static final Map<String, Function<String, String>> operations = new HashMap<>();

    static {
        operations.put("LOGIN", AuthController::login);
        operations.put("LOGOUT", AuthController::logout);
        operations.put("CRIAR_USUARIO", UsuarioController::cadastrar);
        operations.put("LISTAR_USUARIOS", UsuarioController::listar);
    }

    public static JsonElement handle(String request) {
        JsonObject requestObject = JsonHandler.stringToJsonObject(request);
        String operation = JsonHandler.stringToJsonObject(request).get("operacao").getAsString();
        JsonObject responseObject = new JsonObject();

        Function<String, String> handler = operations.get(operation);

        if (handler == null) {

            if (!requestObject.has("operation") ||
                    requestObject.get("operation").isJsonNull() ||
                    requestObject.get("operation").getAsString().isBlank()) {
                responseObject.addProperty("status", StatusCode.BAD_REQUEST);
                responseObject.addProperty("message", StatusCode.getMessage(StatusCode.BAD_REQUEST));
                return responseObject;
            }

            responseObject.addProperty("status", StatusCode.INTERNAL_SERVER_ERROR);
            responseObject.addProperty("message", StatusCode.getMessage(StatusCode.INTERNAL_SERVER_ERROR));
            return responseObject;
        }

        String response = operations.get(operation).apply(request);
        JsonElement jsonResponse = JsonHandler.stringToJson(response);

        return jsonResponse;
    }
}
