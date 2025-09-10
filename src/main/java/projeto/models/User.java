package projeto.models;

public class User {
    private String id;
    private String usuario;
    private String senha;
    private String criadoEm;
    private String role;

    public User(String usuario) {
        this.usuario = usuario;
    }

    public User(String usuario, String senha) {
        this.usuario = usuario;
        this.senha = senha;
    }

    public User(String usuario, String senha, String criadoEm) {
        this.usuario = usuario;
        this.senha = senha;
        this.criadoEm = criadoEm;
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

    public String getId() {
        return this.id;
    }

    public String getRole() {
        return this.role;
    }

    // Setters
    public void setSenha(String senha) {
        this.senha = senha;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
