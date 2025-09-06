package projeto.viewControllers;

import com.google.gson.JsonObject;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.fxml.FXMLLoader;
import projeto.Session;
import projeto.handlers.JsonHandler;
import projeto.handlers.StatusCode;

public class MenuController {

    @FXML
    private void goHome() {
        changeScene("/projeto/views/Home.fxml");
    }

    @FXML
    private void goUsuarios() {
        changeScene("/projeto/views/Usuarios.fxml");
    }

    @FXML
    private void goRelatorios() {
        changeScene("/projeto/views/Relatorios.fxml");
    }

    @FXML
    private void logout() {
        JsonObject logoutResponse = Session.getInstance().desconectar();
        System.out
                .println("\nServidor -> Cliente: " + JsonHandler.prettyFormatFromString(logoutResponse.toString()));

        if (logoutResponse.get("status").getAsString().equals(StatusCode.OK)) {
            try {
                Session.getInstance().clear();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/projeto/views/CONEXAO.fxml"));
                Scene conexaoScene = new Scene(loader.load(), Session.SCENE_WIDTH, Session.SCENE_HEIGHT);
                Stage stage = Session.getInstance().getCurrentStage();
                stage.setScene(conexaoScene);
            } catch (Exception e) {
                System.out.println("Erro ao trocar de tela: " + e.getMessage());
            }
        } else {
            showError(logoutResponse.get("message").getAsString());
        }
    }

    private void changeScene(String fxmlPath) {
        try {
            Stage stage = Session.getInstance().getCurrentStage();
            Scene newScene = new Scene(FXMLLoader.load(getClass().getResource(fxmlPath)), Session.SCENE_WIDTH,
                    Session.SCENE_HEIGHT);
            stage.setScene(newScene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showError(String mensagem) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText("Falha no logout");
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}