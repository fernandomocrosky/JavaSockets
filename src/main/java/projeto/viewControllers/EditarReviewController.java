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
import projeto.models.Review;
import projeto.requests.reviews.EditarReviewPayload;

public class EditarReviewController {
    private Review review;

    public void setReview(Review review) {
        this.review = review;
        preencherCampos();
    }

    @FXML
    private TextArea logArea;

    @FXML
    private TextField tituloField, descricaoField, notaField;

    @FXML
    private Label status;

    private void preencherCampos() {
        if (review != null) {
            tituloField.setText(review.getTitulo() != null ? review.getTitulo() : "");
            descricaoField.setText(review.getDescricao() != null ? review.getDescricao() : "");
            notaField.setText(review.getNota() != null ? review.getNota() : "");
        }
    }

    @FXML
    private void salvar() {
        List<String> errors = Validator.validateFields(List.of("titulo", "descricao", "nota"), tituloField,
                descricaoField, notaField);

        if (!errors.isEmpty()) {
            status.setText(String.join("\n", errors));
            return;
        }

        JsonObject reviewJson = new JsonObject();
        reviewJson.addProperty("id", review.getId());
        reviewJson.addProperty("titulo", tituloField.getText().trim());
        reviewJson.addProperty("descricao", descricaoField.getText().trim());
        reviewJson.addProperty("nota", notaField.getText().trim());

        EditarReviewPayload payload = new EditarReviewPayload(reviewJson);

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
                Session.getInstance().showAlert(AlertType.INFORMATION, "Sucesso",
                        "Review editada com sucesso!", () -> {
                            // Se a review tem id_filme, voltar para VerFilme, senão para Reviews
                            if (review != null && review.getFilme() != null && !review.getFilme().isEmpty()) {
                                SceneHandler.changeSceneWithData("/projeto/views/VerFilme.fxml",
                                        (VerFilmeController controller) -> controller.setFilmeId(review.getFilme()));
                            } else {
                                SceneHandler.changeScene("/projeto/views/Reviews.fxml");
                            }
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
        // Se a review tem id_filme, voltar para VerFilme, senão para Reviews
        if (review != null && review.getFilme() != null && !review.getFilme().isEmpty()) {
            SceneHandler.changeSceneWithData("/projeto/views/VerFilme.fxml",
                    (VerFilmeController controller) -> controller.setFilmeId(review.getFilme()));
        } else {
            SceneHandler.changeScene("/projeto/views/Reviews.fxml");
        }
    }

    @FXML
    private void voltar() {
        cancelar();
    }

    @FXML
    public void initialize() {
        LogUI.init(logArea);
    }

}

