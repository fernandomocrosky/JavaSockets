package projeto.viewControllers;

import java.util.List;

import com.google.gson.JsonObject;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import projeto.Session;
import projeto.Validator;
import projeto.handlers.JsonHandler;
import projeto.handlers.SceneHandler;
import projeto.handlers.StatusCode;
import projeto.models.User;
import projeto.requests.user.EditUserPayload;

public class EditarUsuarioController {
    @FXML
    private TextField usuarioField;
    @FXML
    private Label status;

    private User usuario;

    public void setUsuario(User user) {
        this.usuario = user;
        usuarioField.setText(user.getUsuario());
    }

    @FXML
    private void salvar() {
        List<String> errors = Validator.validateFields(List.of("usuario"), usuarioField);

        if (!errors.isEmpty()) {
            status.setText(String.join("\n", errors));
            return;
        }

        EditUserPayload payload = new EditUserPayload(this.usuario);
        String msg = JsonHandler.modelToString(payload);

        Session.getInstance().getOut().println(msg);
        String response;

        try {
            response = Session.getInstance().getIn().readLine();
            JsonObject responseJson = JsonHandler.stringToJsonObject(response);

            if (responseJson.get("status").getAsString().equals(StatusCode.OK)) {
                SceneHandler.changeScene("/projeto/views/Usuarios.fxml");
                Session.getInstance().showAlert(AlertType.CONFIRMATION, "Salvo com sucesso",
                        "Usuário salvo com sucesso!",
                        () -> SceneHandler.changeScene("/projeto/views/Usuarios.fxml"));
            }
        } catch (Exception ex) {
            Session.getInstance().showAlert(AlertType.ERROR, "Erro", "Erro ao salvar usuário", () -> {
            });
            System.err.println("Erro na comunicação com o servidor: " + ex.getMessage());
            return;
        }
    }

    @FXML
    private void cancelar() {
        SceneHandler.changeScene("/projeto/views/Usuarios.fxml");
    }
}
