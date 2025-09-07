package projeto.viewControllers;

import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import projeto.Session;
import projeto.Validator;
import projeto.handlers.SceneHandler;
import projeto.models.User;

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

        String usuario = usuarioField.getText();
        System.out.println("Salvar -> " + usuario);
        
        Session.getInstance().showAlert(AlertType.CONFIRMATION, "Salvo com sucesso", "Usuário salvo com sucesso!");
    }

    @FXML
    private void cancelar() {
        SceneHandler.changeScene("/projeto/views/Usuarios.fxml");
    }
}
