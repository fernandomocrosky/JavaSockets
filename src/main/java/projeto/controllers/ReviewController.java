package projeto.controllers;

import com.google.gson.JsonObject;

import projeto.handlers.JsonHandler;
import projeto.handlers.StatusCode;

public class ReviewController {
    public static String cadastrar(String request) {
        JsonObject requestJson = JsonHandler.stringToJsonObject(request);
        JsonObject responseObject = new JsonObject();

        // TODO cadastro review abre conexão banco de dados etc

        responseObject.addProperty("status", StatusCode.OK);
        responseObject.addProperty("message", StatusCode.getMessage(StatusCode.OK));
        
        return JsonHandler.jsonToString(responseObject);
    }
}
