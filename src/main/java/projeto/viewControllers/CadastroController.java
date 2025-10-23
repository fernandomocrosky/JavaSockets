package projeto.viewControllers;

import java.util.List;

import com.google.gson.JsonObject;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import projeto.LogUI;
import projeto.Session;
import projeto.Validator;
import projeto.handlers.JsonHandler;
import projeto.handlers.SceneHandler;
import projeto.handlers.StatusCode;
import projeto.requests.CadastroPayload;

public class CadastroController {

    @FXML
    private TextArea logArea;

    @FXML
    private TextField userField;

    @FXML
    private PasswordField passField;

    @FXML
    private Label status;

    @FXML
    private void cadastrar() {
        if (!Session.getInstance().isConnected()) {
            Session.getInstance().showAlert(AlertType.ERROR, "Erro", "Nenhuma conexão ativa", () -> {
            });
            return;
        }

        List<String> errors = Validator.validateFields(List.of("usuario", "senha"), userField, passField);

        if (!errors.isEmpty()) {
            status.setText(String.join("\n", errors));
            return;
        }

        // cria objeto de usuário e request
        JsonObject user = new JsonObject();
        user.addProperty("nome", userField.getText());
        user.addProperty("senha", passField.getText());

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
        System.out.println("\nServidor -> Cliente: " + JsonHandler.prettyFormatFromString(response));
        LogUI.log("Cliente -> Servidor: " + JsonHandler.prettyFormatFromString(msg));
        LogUI.log("Servidor -> Cliente: " + JsonHandler.prettyFormatFromString(response));
        if (response != null && !response.isEmpty()
                && responseJson.get("status").getAsString().equals(StatusCode.CREATED)) {
            System.out.println("\nServidor -> Cliente: " + JsonHandler.prettyFormatFromString(response));
            try {
                SceneHandler.changeScene("/projeto/views/LOGIN.fxml");
            } catch (Exception ex) {
                System.out.println("Erro ao trocar de tela: " + ex.getMessage());
            }
        } else {
            status.setText(StatusCode.getMessage(responseJson.get("status").getAsString()));
        }
    }

    @FXML
    public void voltar() {
        try {
            SceneHandler.changeScene("/projeto/views/LOGIN.fxml");
        } catch (Exception e) {
            System.out.println("Erro ao trocar de tela: " + e.getMessage());
        }
    }

    @FXML
    public void initialize() {
        LogUI.init(logArea);
    }
}
