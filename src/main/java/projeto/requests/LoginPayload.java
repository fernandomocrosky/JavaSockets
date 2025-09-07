package projeto.requests;

import projeto.models.User;

public class LoginPayload {
    public String usuario;
    public String senha;
    public final String operacao = "login".toUpperCase();

    public LoginPayload(User user) {
        this.usuario = user.getUsuario();
        this.senha = user.getSenha();
    }
}
