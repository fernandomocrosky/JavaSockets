package projeto.requests.user;

import projeto.Session;

public class DeleteUserPayload {
    public final String operacao = "EXCLUIR_USUARIO".toUpperCase();
    public final String token = Session.getInstance().getToken();
    public final String id;

    public DeleteUserPayload(String id) {
        this.id = id;
    }
}
