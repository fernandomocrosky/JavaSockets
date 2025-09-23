package projeto.requests;

import com.google.gson.JsonObject;

import projeto.models.User;

public class CadastroPayload {
    public final JsonObject usuario;
    public final String operacao = "criar_usuario".toUpperCase();

    public CadastroPayload(JsonObject usuario) {
        this.usuario = usuario;
    }
}
