package projeto.viewControllers;

import java.util.List;

import com.google.gson.JsonObject;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import projeto.LogUI;
import projeto.Session;
import projeto.Validator;
import projeto.handlers.JsonHandler;
import projeto.handlers.SceneHandler;
import projeto.handlers.StatusCode;
import projeto.requests.filmes.CadastroFilmePayload;

public class CadastroFilmeController {

    @FXML
    private TextArea logArea;

    @FXML
    private ListView<String> generosList;

    @FXML
    private TextField tituloField, diretorField, anoField, sinopseField;

    @FXML
    private Label status;

    @FXML
    public void initialize() {
        LogUI.init(logArea);

        generosList.getItems().addAll(
                "Ação",
                "Aventura",
                "Comédia",
                "Drama",
                "Fantasia",
                "Ficção Científica",
                "Terror",
                "Romance",
                "Documentário",
                "Musical",
                "Animação");

        // permite selecionar vários de uma vez
        generosList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    @FXML
    private void cadastrar() {
        List<String> generosSelecionados = generosList.getSelectionModel().getSelectedItems();

        List<String> errors = Validator.validateFields(List.of("titulo", "diretor", "ano", "sinopse"), tituloField,
                diretorField,
                anoField,
                sinopseField);

        if (generosSelecionados == null || generosSelecionados.isEmpty()) {
            errors.add("Selecione pelo menos um genero");
        }

        if (!errors.isEmpty()) {
            status.setText(String.join("\n", errors));
            return;
        }

        System.out.println(generosSelecionados);

        JsonObject filme = new JsonObject();

        filme.addProperty("titulo", tituloField.getText().trim());
        filme.addProperty("diretor", diretorField.getText().trim());
        filme.addProperty("ano", anoField.getText().trim());
        filme.addProperty("sinopse", sinopseField.getText().trim());
        JsonHandler.addArray(filme, "genero", generosSelecionados);

        CadastroFilmePayload payload = new CadastroFilmePayload(filme);
        String msg = JsonHandler.modelToString(payload);
        Session.getInstance().getOut().println(msg);
        System.out.println("Cliente -> Servidor: " + JsonHandler.prettyFormatFromString(msg));
        LogUI.log("Cliente -> Servidor: " + JsonHandler.prettyFormatFromString(msg));
        String response = null;

        try {
            response = Session.getInstance().getIn().readLine();
            System.out.println("Servidor -> Cliente: " + response);
        } catch (Exception ex) {
            System.err.println("Erro na comunicação com o servidor: " + ex.getMessage());
        }

        JsonObject responseJson = JsonHandler.stringToJsonObject(response);
        System.out.println("Servidor -> Cliente: "
                + JsonHandler.prettyFormatFromString(JsonHandler.prettyFormatFromJson(responseJson)));
        LogUI.log("Servidor -> Cliente: "
                + JsonHandler.prettyFormatFromString(JsonHandler.prettyFormatFromJson(responseJson)));
        if (response != null && !response.isEmpty()
                && responseJson.get("status").getAsString().equals(StatusCode.CREATED)) {
            Session.getInstance().showAlert(AlertType.CONFIRMATION, "Salvo com sucesso", "Filme salvo com sucesso!",
                    () -> {
                        try {
                            SceneHandler.changeScene("/projeto/views/Home.fxml");

                        } catch (Exception e) {
                            System.out.println("Erro ao trocar de tela: " + e.getMessage());
                        }
                    });
        } else {
            status.setText(StatusCode.getMessage(responseJson.get("status").getAsString()));
        }
    }

    @FXML
    private void voltar() {
        try {
            SceneHandler.changeScene("/projeto/views/Home.fxml");
        } catch (Exception e) {
            System.out.println("Erro ao trocar de tela: " + e.getMessage());
        }
    }

    @FXML
    private void cancelar() {
        this.voltar();
    }
}
