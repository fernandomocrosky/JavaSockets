package projeto.requests.filmes;

import projeto.Session;

public class ListFilmePayload {
    public final String operacao = "LISTAR_FILMES".toUpperCase();
    public final String token = Session.getInstance().getToken();

    public ListFilmePayload() {
    }
}