package projeto.requests.user;

import projeto.Session;

public class EditUserPayload {
    public final String operacao = "EDITAR_USUARIO".toUpperCase();
    public final String token = Session.getInstance().getToken();

}
