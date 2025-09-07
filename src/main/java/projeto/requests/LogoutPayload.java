package projeto.requests;

public class LogoutPayload {
    public final String operacao = "logout".toUpperCase();
    public final String token;

    public LogoutPayload(String token) {
        this.token = token;
    }
}
