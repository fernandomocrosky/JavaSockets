package projeto.requests.filmes;

import projeto.Session;

public class DeleteFilmePayload {
    private String operacao = "EXCLUIR_FILME".toUpperCase();
    private String token = Session.getInstance().getToken();
    private String id;

    public DeleteFilmePayload(String id) {
        this.id = id;
    }
}
