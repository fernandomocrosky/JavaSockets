package projeto.requests.user;

import com.google.gson.JsonObject;

import projeto.Session;
import projeto.models.User;

public class AdminEditUserPayload {
    public final String operacao = "ADMIN_EDITAR_USUARIO".toUpperCase();
    public final String token = Session.getInstance().getToken();
    public final String id;
    public final JsonObject usuario;

    public AdminEditUserPayload(User user) {
        this.id = user.getId();
        usuario = new JsonObject();
        usuario.addProperty("senha", user.getSenha());
    }
}
