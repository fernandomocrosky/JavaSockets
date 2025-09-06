package projeto.viewControllers;

import com.google.gson.JsonObject;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import projeto.Session;
import projeto.handlers.JsonHandler;
import projeto.handlers.JwtHandle;
import projeto.handlers.StatusCode;

public class HomeController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label infoLabel;

    @FXML
    public void initialize() {
        // pega o usuário atual da Session
        String token = Session.getInstance().getToken();
        if (token != null) {
            String usuario = JwtHandle.getClaim(Session.getInstance().getToken(), "usuario", String.class);

            welcomeLabel.setText("Olá, " + usuario + "!");
            infoLabel.setText(
                    "Seu token JWT: " + (token != null ? token.substring(0, 15) + "..." : "não encontrado"));
        } else {
            welcomeLabel.setText("Olá, visitante!");
            infoLabel.setText("Nenhum token encontrado.");
        }
    }

    // @FXML
    // private void logout() {
    //     JsonObject logoutResponse = Session.getInstance().desconectar();
    //     System.out
    //             .println("\nServidor -> Cliente: " + JsonHandler.prettyFormatFromString(logoutResponse.toString()));

    //     if (logoutResponse.get("status").getAsString().equals(StatusCode.OK)) {
    //         try {
    //             FXMLLoader loader = new FXMLLoader(getClass().getResource("/projeto/views/CONEXAO.fxml"));
    //             Scene conexaoScene = new Scene(loader.load(), 420, 420);
    //             Stage stage = (Stage) welcomeLabel.getScene().getWindow();
    //             stage.setScene(conexaoScene);
    //         } catch (Exception e) {
    //             System.out.println("Erro ao trocar de tela: " + e.getMessage());
    //         }
    //     } else {
    //         showError(logoutResponse.get("message").getAsString());
    //     }
    // }
}
