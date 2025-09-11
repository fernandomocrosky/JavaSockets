package projeto;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Optional;

import com.google.gson.JsonObject;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import projeto.dao.UserDAO;
import projeto.handlers.JsonHandler;
import projeto.handlers.JwtHandle;
import projeto.handlers.StatusCode;
import projeto.models.User;
import projeto.requests.LogoutPayload;

public class Session {
    private static final Session instance = new Session();

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private User user;

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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public String getRole() {
        return this.getUser().getRole();
    }

    public Boolean isAdmin() {
        return this.user.getRole().equals("admin");
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

        try {
            this.socket.close();
            this.in.close();
            this.out.close();
            this.token = null;

            this.socket = null;
            this.out = null;
            this.in = null;
            this.token = null;
        } catch (Exception e) {
            System.out.println("Erro ao desconectar: " + e.getMessage());
        }

    }

    public static boolean validateToken(String token) {
        try {
            JwtHandle.validateToken(token);

            return !UserDAO.isTokenBlacklisted(token);
        } catch (Exception e) {
            return false;
        }
    }

    public void showAlert(AlertType type, String title, String message, Runnable onOk) {
        Alert alert = new Alert(type, message, ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle(title);
        alert.setHeaderText(null);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (onOk != null)
                onOk.run();
        }
    }

    public JsonObject desconectar() {
        JsonObject response = new JsonObject();
        LogoutPayload logoutBody = new LogoutPayload(Session.getInstance().getToken());
        String payload = JsonHandler.modelToString(logoutBody);
        System.out.println("Cliente -> Servidor: " + JsonHandler.prettyFormatFromString(payload));
        LogUI.log("Cliente -> Servidor: " + JsonHandler.prettyFormatFromString(payload));
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
