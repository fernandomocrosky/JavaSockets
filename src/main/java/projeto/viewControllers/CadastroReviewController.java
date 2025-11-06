package projeto.viewControllers;

import java.util.List;

import com.google.gson.JsonObject;

import javafx.fxml.FXML;
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
import projeto.requests.reviews.CadastroReviewPayload;

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

        JsonObject review = new JsonObject();
        review.addProperty("id_filme", filme.getId());
        review.addProperty("titulo", tituloField.getText());
        review.addProperty("descricao", descricaoField.getText());
        review.addProperty("nota", notaField.getText());

        CadastroReviewPayload payload = new CadastroReviewPayload(review);

        String msg = JsonHandler.modelToString(payload);
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
            
            String statusCode = responseJson.get("status").getAsString();
            if (response != null && statusCode.equals(StatusCode.OK)) {
                Session.getInstance().showAlert(AlertType.CONFIRMATION, "Sucesso",
                        "Review criada com sucesso!", () -> {
                            SceneHandler.changeScene("/projeto/views/Filmes.fxml");
                        });
            } else {
                status.setText(StatusCode.getMessage(statusCode));
            }
        } catch (Exception ex) {
            System.out.println("Erro ao se comunicar com o servidor\n" + ex.getMessage());
            status.setText("Erro ao se comunicar com o servidor: " + ex.getMessage());
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
