package projeto.requests.filmes;

import com.google.gson.JsonObject;

import projeto.Session;

public class EditarFilmePayload {
    public final String operacao = "EDITAR_FILME".toUpperCase();
    public final String token = Session.getInstance().getToken();

    private JsonObject filme;

    public EditarFilmePayload(JsonObject filme) {
        this.filme = filme;
    }
}
