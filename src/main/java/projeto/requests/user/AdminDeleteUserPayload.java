package projeto.requests.user;

import projeto.Session;

public class AdminDeleteUserPayload {
    public final String operacao = "ADMIN_EXCLUIR_USUARIO".toUpperCase();
    public final String token = Session.getInstance().getToken();
    public final String id;

    public AdminDeleteUserPayload(String id) {
        this.id = id;
    }
}
