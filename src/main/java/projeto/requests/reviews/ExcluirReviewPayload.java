package projeto.requests.reviews;

import projeto.Session;

public class ExcluirReviewPayload {
    private String operacao = "EXCLUIR_REVIEW".toUpperCase();
    private String token = Session.getInstance().getToken();
    private String id;

    public ExcluirReviewPayload(String id) {
        this.id = id;
    }
}

