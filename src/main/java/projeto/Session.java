package projeto;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.google.gson.JsonObject;

import io.jsonwebtoken.Jwts;
import javafx.stage.Stage;
import projeto.dao.UserDAO;
import projeto.handlers.JsonHandler;
import projeto.handlers.JwtHandle;
import projeto.handlers.StatusCode;
import projeto.requests.LogoutPayload;

public class Session {
    private static final Session instance = new Session();

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private String token;

    private Stage currentStage;
    public static final double SCENE_WIDTH = 1024;
    public static final double SCENE_HEIGHT = 768;

    private Session() {
    }

    // setter para Stage
    public void setCurrentStage(Stage stage) {
        this.currentStage = stage;
    }

    public Stage getCurrentStage() {
        return currentStage;
    }

    public static Session getInstance() {
        return instance;
    }

    public void setConnection(Socket socket, PrintWriter out, BufferedReader in) {
        this.socket = socket;
        this.out = out;
        this.in = in;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean hasToken() {
        return token != null && !token.isEmpty();
    }

    public String getToken() {
        return token;
    }

    public Socket getSocket() {
        return socket;
    }

    public PrintWriter getOut() {
        return out;
    }

    public BufferedReader getIn() {
        return in;
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public void clear() {
        this.socket = null;
        this.out = null;
        this.in = null;
        this.token = null;
    }

    public static boolean validateToken(String token) {
        try {
            JwtHandle.validateToken(token);

            return !UserDAO.isTokenBlacklisted(token);
        } catch (Exception e) {
            return false;
        }
    }

    public JsonObject desconectar() {
        JsonObject response = new JsonObject();
        LogoutPayload logoutBody = new LogoutPayload(Session.getInstance().getToken());
        String payload = JsonHandler.modelToString(logoutBody);
        System.out.println("Cliente -> Servidor: " + JsonHandler.prettyFormatFromString(payload));

        out.println(payload);

        try {
            response = JsonHandler.stringToJsonObject(in.readLine());
            response.addProperty("message", StatusCode.getMessage(response.get("status").getAsString()));
        } catch (Exception ex) {
            System.err.println("Erro na comunicação com o servidor: " + ex.getMessage());
            response.addProperty("status", StatusCode.INTERNAL_SERVER_ERROR);
            response.addProperty("message", StatusCode.getMessage(StatusCode.INTERNAL_SERVER_ERROR));
        }

        return response;
    }
}
