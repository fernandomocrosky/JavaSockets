package projeto.requests.user;

import projeto.Session;

public class MyUserPayload {
    public final String operacao = "LISTAR_PROPRIO_USUARIO".toUpperCase();
    public final String token = Session.getInstance().getToken();

    public MyUserPayload() {}
}
