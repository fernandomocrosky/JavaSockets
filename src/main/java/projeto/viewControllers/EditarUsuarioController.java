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
import projeto.models.User;
import projeto.requests.user.EditUserPayload;

public class EditarUsuarioController {

    @FXML
    private TextArea logArea;
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

        User usuario = new User();
        usuario.setUsuario(usuarioField.getText());
        usuario.setId(this.usuario.getId());

        EditUserPayload payload = new EditUserPayload(usuario);
        String msg = JsonHandler.modelToString(payload);
        System.out.println("Cliente -> Servidor: " + JsonHandler.prettyFormatFromString(msg));
        LogUI.log("Cliente -> Servidor: " + JsonHandler.prettyFormatFromString(msg));
        Session.getInstance().getOut().println(msg);
        String response;

        try {
            response = Session.getInstance().getIn().readLine();
            JsonObject responseJson = JsonHandler.stringToJsonObject(response);
            System.out.println("Servidor -> Cliente: " + JsonHandler.prettyFormatFromString(responseJson.toString()));
            LogUI.log("Servidor -> Cliente: " + JsonHandler.prettyFormatFromString(responseJson.toString()));
            if (responseJson.get("status").getAsString().equals(StatusCode.OK)) {
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

    @FXML
    public void initialize() {
        LogUI.init(logArea);
    }
}
