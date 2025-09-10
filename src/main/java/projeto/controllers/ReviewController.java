package projeto.controllers;

import com.google.gson.JsonObject;

import projeto.handlers.JsonHandler;

public class ReviewController {
    public static String cadastrar(String request) {
        JsonObject requestJson = JsonHandler.stringToJsonObject(request);
        JsonObject responseObject = new JsonObject();

        // TODO cadastro review abre conexão etc
        
        return JsonHandler.jsonToString(responseObject);
    }
}
