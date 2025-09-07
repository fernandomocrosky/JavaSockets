package projeto.requests.user;

import projeto.Session;

public class ListUserPayload {
    public final String operacao = "listar_usuarios".toUpperCase();
    public final String token = Session.getInstance().getToken();

    public ListUserPayload() {
    }
}
