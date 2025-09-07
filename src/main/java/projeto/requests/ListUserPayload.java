package projeto.requests;

import projeto.Session;

public class ListUserPayload {
    public final String operacao = "listar_usuarios".toUpperCase();
    public final String token;

    public ListUserPayload(String token) {
        this.token = Session.getInstance().getToken();
    }
}
