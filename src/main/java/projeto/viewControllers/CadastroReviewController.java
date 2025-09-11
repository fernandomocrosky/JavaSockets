package projeto.viewControllers;

import java.util.List;

import com.google.gson.JsonObject;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import projeto.LogUI;
import projeto.Session;
import projeto.Validator;
import projeto.handlers.JsonHandler;
import projeto.handlers.SceneHandler;
import projeto.handlers.StatusCode;
import projeto.models.Filme;

public class CadastroReviewController {
    private Filme filme;

    public void setFilme(Filme filme) {
        this.filme = filme;
    }

    @FXML
    private TextArea logArea;

    @FXML
    private TextField tituloField, descricaoField, notaField;

    @FXML
    private Label status;

    @FXML
    private void avaliar() {
        List<String> errors = Validator.validateFields(List.of("titulo", "descricao", "nota"), tituloField,
                descricaoField, notaField);

        if (!errors.isEmpty()) {
            status.setText(String.join("\n", errors));
            return;
        }

        JsonObject request = new JsonObject();
        request.addProperty("id_filme", filme.getId());
        request.addProperty("titulo", tituloField.getText());
        request.addProperty("descricao", descricaoField.getText());
        request.addProperty("nota", notaField.getText());

        String msg = JsonHandler.modelToString(request);
        try {
            Session.getInstance().getOut().println(msg);
            System.out.println("Cliente -> Servidor: " + JsonHandler.prettyFormatFromString(msg));
            LogUI.log("Cliente -> Servidor: " + JsonHandler.prettyFormatFromString(msg));
        } catch (Exception ex) {
            System.out.println("Erro ao enviar review\n" + ex.getMessage());
        }

        String response = null;

        try {
            response = Session.getInstance().getIn().readLine();
            JsonObject responseJson = JsonHandler.stringToJsonObject(response);
            System.out.println("Servidor -> Cliente: " + JsonHandler.prettyFormatFromString(responseJson.toString()));
            LogUI.log("Servidor -> Cliente: " + JsonHandler.prettyFormatFromString(responseJson.toString()));
            if (response != null && responseJson.get("status").getAsString().equals(StatusCode.CREATED)) {
                Session.getInstance().showAlert(AlertType.CONFIRMATION, "Review enviada com sucesso",
                        "Review enviada com sucesso", () -> {
                            SceneHandler.changeScene("/projeto/views/Reviews.fxml");
                        });
            } else {
                status.setText(StatusCode.getMessage(responseJson.get("status").getAsString()));
            }
        } catch (Exception ex) {
            System.out.println("Erro ao se comunicar com o servidor\n" + ex.getMessage());
        }

        return;
    }

    @FXML
    private void cancelar() {
        SceneHandler.changeScene("/projeto/views/Filmes.fxml");
    }

    @FXML
    private void voltar() {
        cancelar();
    }

    public void initialize() {
        LogUI.init(logArea);
    }

}
