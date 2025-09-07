package projeto.viewControllers;

import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import projeto.Session;
import projeto.Validator;
import projeto.handlers.JsonHandler;
import projeto.handlers.SceneHandler;
import projeto.handlers.StatusCode;
import projeto.models.User;
import projeto.requests.LoginPayload;

public class LoginController {

    @FXML
    private TextField userField;

    @FXML
    private PasswordField passField;

    @FXML
    private Label status;

    @FXML
    private void login() {
        if (!Session.getInstance().isConnected()) {
            status.setText("Nenhuma conexão ativa");
            return;
        }

        List<String> errors = Validator.validateFields(List.of("usuario", "senha"), userField, passField);

        if (!errors.isEmpty()) {
            status.setText(String.join("\n", errors));
            return;
        }

        // cria objeto de usuário e request
        User user = new User(userField.getText(), passField.getText());
        LoginPayload requestBody = new LoginPayload(user);

        // transforma em JSON
        String msg = JsonHandler.modelToString(requestBody);

        // envia para o servidor
        Session.getInstance().getOut().println(msg);
        String response = null;

        try {
            response = Session.getInstance().getIn().readLine();
        } catch (Exception ex) {
            System.err.println("Erro na comunicação com o servidor: " + ex.getMessage());
        }

        JsonObject responseJson = JsonHandler.stringToJsonObject(response);

        status.setText("Login enviado!");
        System.out.println("Cliente -> Servidor: " + JsonHandler.prettyFormatFromString(msg));
        if (response != null && !response.isEmpty() && responseJson.get("status").getAsString().equals(StatusCode.OK)) {
            System.out.println("\nServidor -> Cliente: " + JsonHandler.prettyFormatFromString(response));
            Session.getInstance().setToken(responseJson.get("token").getAsString());

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/projeto/views/HOME.fxml"));
                Scene homeScene = new Scene(loader.load(), SceneHandler.SCENE_WIDTH, SceneHandler.SCENE_HEIGHT);
                Stage stage = (Stage) userField.getScene().getWindow();
                stage.setScene(homeScene);
            } catch (Exception e) {
                System.out.println("Erro ao trocar de tela: " + e.getMessage());
            }
        } else {
            status.setText(responseJson.get("message").getAsString());
        }
    }

    @FXML
    private void cadastrar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/projeto/views/CADASTRO.fxml"));
            Scene cadastroScene = new Scene(loader.load(), SceneHandler.SCENE_WIDTH, SceneHandler.SCENE_HEIGHT);
            Stage stage = (Stage) userField.getScene().getWindow();
            stage.setScene(cadastroScene);
        } catch (Exception e) {
            System.out.println("Erro ao trocar de tela: " + e.getMessage());
        }
    }
}
