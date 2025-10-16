package projeto.viewControllers;

import com.google.gson.JsonObject;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert.AlertType;
import projeto.LogUI;
import projeto.Session;
import projeto.handlers.JsonHandler;
import projeto.handlers.SceneHandler;
import projeto.handlers.StatusCode;
import projeto.requests.user.MyUserPayload;

public class MeuUsuarioController {

    @FXML
    private TextArea logArea;

    @FXML
    private Label usuarioLabel;

    @FXML
    private void initialize() {
        LogUI.init(logArea);
        usuarioLabel.setText(Session.getInstance().getUser().getUsuario());
        
        MyUserPayload payload = new MyUserPayload();
        
        String msg = JsonHandler.modelToString(payload);

        System.out.println("Cliente -> Servidor: " + JsonHandler.prettyFormatFromString(msg));
        LogUI.log("Cliente -> Servidor: " + JsonHandler.prettyFormatFromString(msg));
        try {
            Session.getInstance().getOut().println(msg);
            JsonObject response = JsonHandler.stringToJsonObject(Session.getInstance().getIn().readLine());
            System.out.println("Servidor -> Cliente: " + JsonHandler.prettyFormatFromString(response.toString()));
            LogUI.log("Servidor -> Cliente: " + JsonHandler.prettyFormatFromString(response.toString()));

            if(response.get("status").getAsString().equals(StatusCode.OK)) {
                Session.getInstance().showAlert(AlertType.INFORMATION, "Sucesso", "Usuário carregado com sucesso", null);
                Session.getInstance().getUser().setUsuario(response.get("usuario").getAsString());
                usuarioLabel.setText(response.get("message").getAsString());
            } else {
                Session.getInstance().showAlert(AlertType.ERROR, "Erro", "Erro ao carregar usuário", () -> SceneHandler.changeScene("/projeto/views/HOME.fxml"));
            }
        } catch (Exception ex) {
            System.err.println("Erro ao buscar meu usuário\n" + ex.getMessage());
        }

    }

    @FXML
    public void voltar() {
        try {
            SceneHandler.changeScene("/projeto/views/HOME.fxml");
        } catch (Exception e) {
            System.out.println("Erro ao trocar de tela: " + e.getMessage());
        }
    }
}
