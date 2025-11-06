package projeto.requests.filmes;

import projeto.Session;

public class GetFilmePayload {
    private String operacao = "LISTAR_FILME_POR_ID".toUpperCase();
    private String token = Session.getInstance().getToken();
    private String id;

    public GetFilmePayload(String id) {
        this.id = id;
    }
}

