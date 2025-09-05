package projeto.requests;

import projeto.models.User;

public class LoginRequest {
    public String usuario;
    public String senha;
    public final String operacao = "login".toUpperCase();

    public LoginRequest(User user) {
        this.usuario = user.getUsuario();
        this.senha = user.getSenha();
    }
}
