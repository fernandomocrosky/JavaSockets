package projeto.requests.reviews;

import com.google.gson.JsonObject;

import projeto.Session;

public class CadastroReviewPayload {
    private String operacao = "CRIAR_REVIEW".toUpperCase();
    private String token = Session.getInstance().getToken();
    private JsonObject review;

    public CadastroReviewPayload(JsonObject review) {
        this.review = review;
    }

}
