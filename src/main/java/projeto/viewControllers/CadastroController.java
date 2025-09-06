package projeto.viewControllers;

import java.util.List;

import com.google.gson.JsonObject;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import projeto.Session;
import projeto.Validator;
import projeto.handlers.JsonHandler;
import projeto.handlers.StatusCode;
import projeto.models.User;
import projeto.requests.CadastroPayload;

public class CadastroController {

    @FXML
    private TextField userField;

    @FXML
    private PasswordField passField;

    @FXML
    private Label status;

    @FXML
    private void cadastrar() {
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
        CadastroPayload requestBody = new CadastroPayload(user);

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
        if (response != null && !response.isEmpty()
                && responseJson.get("status").getAsString().equals(StatusCode.CREATED)) {
            System.out.println("\nServidor -> Cliente: " + JsonHandler.prettyFormatFromString(response));
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/projeto/views/LOGIN.fxml"));
                Scene homeScene = new Scene(loader.load(), 420, 300);
                Stage stage = (Stage) userField.getScene().getWindow();
                stage.setScene(homeScene);
            } catch (Exception e) {
                System.out.println("Erro ao trocar de tela: " + e.getMessage());
            }
        } else {
            status.setText(StatusCode.getMessage(responseJson.get("status").getAsString()));
        }
    }

    @FXML
    public void voltar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/projeto/views/LOGIN.fxml"));
            Scene loginScene = new Scene(loader.load(), 420, 420);
            Stage stage = (Stage) userField.getScene().getWindow();
            stage.setScene(loginScene);
        } catch (Exception e) {
            System.out.println("Erro ao trocar de tela: " + e.getMessage());
        }
    }
}
