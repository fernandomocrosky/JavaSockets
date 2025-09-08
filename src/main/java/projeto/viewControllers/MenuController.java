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
import projeto.handlers.SceneHandler;
import projeto.handlers.StatusCode;

public class MenuController {

    @FXML
    private void goHome() {
       SceneHandler.changeScene("/projeto/views/Home.fxml");
    }

    @FXML
    private void goUsuarios() {
        SceneHandler.changeScene("/projeto/views/Usuarios.fxml");
    }

    // @FXML
    // private void goFilmes() {
    //     SceneHandler.changeScene("/projeto/views/Filmes.fxml");
    // }

    @FXML
    private void logout() {
        JsonObject logoutResponse = Session.getInstance().desconectar();
        System.out
                .println("\nServidor -> Cliente: " + JsonHandler.prettyFormatFromString(logoutResponse.toString()));

        if (logoutResponse.get("status").getAsString().equals(StatusCode.OK)) {
            try {
                Session.getInstance().clear();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/projeto/views/CONEXAO.fxml"));
                Scene conexaoScene = new Scene(loader.load(), SceneHandler.SCENE_WIDTH, SceneHandler.SCENE_HEIGHT);
                Stage stage = Session.getInstance().getCurrentStage();
                stage.setScene(conexaoScene);
            } catch (Exception e) {
                System.out.println("Erro ao trocar de tela: " + e.getMessage());
            }
        } else {
            showError(logoutResponse.get("message").getAsString());
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