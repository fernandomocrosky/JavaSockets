package projeto.requests;

import projeto.models.User;

public class CadastroPayload {
    public String usuario;
    public String senha;
    public final String operacao = "criar_usuario".toUpperCase();

    public CadastroPayload(User user) {
        this.usuario = user.getUsuario();
        this.senha = user.getSenha();
    }
}
