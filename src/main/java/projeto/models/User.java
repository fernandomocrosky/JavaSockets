package projeto.models;

public class User {
    private String usuario;
    private String senha;
    private String token;

    public User(String usuario, String senha, String token) {
        this.usuario = usuario;
        this.senha = senha;
    }

    public User() {
    }

    // Getters

    public String getSenha() {
        return senha;
    }

    public String getUsuario() {
        return usuario;
    }

    // Setters
    public void setSenha(String senha) {
        this.senha = senha;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }
}
