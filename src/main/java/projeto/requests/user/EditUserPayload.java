package projeto.requests.user;

import com.google.gson.JsonObject;

import projeto.Session;
import projeto.models.User;

public class EditUserPayload {
    public final String operacao = "EDITAR_USUARIO".toUpperCase();
    public final String token = Session.getInstance().getToken();
    public final JsonObject usuario;

    public EditUserPayload(User user) {
        usuario = new JsonObject();
        usuario.addProperty("id", user.getId());
        usuario.addProperty("nome", user.getUsuario());
    }
}
