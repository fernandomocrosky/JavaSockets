package projeto.requests.user;

import projeto.Session;
import projeto.models.User;

public class EditUserPayload {
    public final String operacao = "EDITAR_USUARIO".toUpperCase();
    public final String token = Session.getInstance().getToken();
    public final User user;

    public EditUserPayload(User user) {
        this.user = user;
    }

}
