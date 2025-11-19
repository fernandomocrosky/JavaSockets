package projeto.requests.reviews;

import projeto.Session;

public class ListReviewsUsuarioPayload {
    public final String operacao = "LISTAR_REVIEWS_USUARIO".toUpperCase();
    public final String token = Session.getInstance().getToken();

    public ListReviewsUsuarioPayload() {
    }
}

