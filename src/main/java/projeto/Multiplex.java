package projeto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import projeto.controllers.LoginController;
import projeto.controllers.UsuarioController;
import projeto.handlers.JsonHandler;
import projeto.handlers.JwtHandle;
import projeto.handlers.StatusCode;

public class Multiplex {
    private static final Map<String, Function<String, String>> operations = new HashMap<>();
    private static final List<String> userPermissions = List.of("LOGOUT");
    private static final List<String> adminPermissions = List.of("LOGOUT");

    static {
        operations.put("LOGIN", LoginController::login);
        operations.put("LOGOUT", LoginController::logout);
        operations.put("CRIAR_USUARIO", UsuarioController::cadastrar);
    }

    public static JsonElement handle(String request) {
        JsonObject requestObject = JsonHandler.stringToJsonObject(request);
        String operation = JsonHandler.stringToJsonObject(request).get("operacao").getAsString();

        Function<String, String> handler = operations.get(operation);

        if (handler == null) {
            JsonObject response = new JsonObject();
            response.addProperty("status", StatusCode.BAD_REQUEST);
            response.addProperty("message", StatusCode.getMessage(StatusCode.BAD_REQUEST));
            return response;
        }

        String response = operations.get(operation).apply(request);
        JsonElement jsonResponse = JsonHandler.stringToJson(response);

        return jsonResponse;
    }
}
