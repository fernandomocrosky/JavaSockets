package projeto.requests.reviews;

import com.google.gson.JsonObject;

import projeto.Session;

public class EditarReviewPayload {
    private String operacao = "EDITAR_REVIEW".toUpperCase();
    private String token = Session.getInstance().getToken();
    private JsonObject review;

    public EditarReviewPayload(JsonObject review) {
        this.review = review;
    }
}

