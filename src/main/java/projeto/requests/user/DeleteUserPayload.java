package projeto.requests.user;

import projeto.Session;

public class DeleteUserPayload {
    public final String operacao = "EXCLUIR_PROPRIO_USUARIO".toUpperCase();
    public final String token = Session.getInstance().getToken();

    public DeleteUserPayload() {
    }
}
