package projeto.requests.filmes;

import com.google.gson.JsonObject;

import projeto.Session;

public class CadastroFilmePayload {
    public final String operacao = "CRIAR_FILME".toUpperCase();
    public final String token = Session.getInstance().getToken();
    public JsonObject filme;

    public CadastroFilmePayload(JsonObject filme) {
        this.filme = filme;
    }
}
