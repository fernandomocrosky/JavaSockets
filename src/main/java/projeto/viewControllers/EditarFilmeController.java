package projeto.viewControllers;

import java.util.List;

import com.google.gson.JsonObject;

import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import projeto.LogUI;
import projeto.Session;
import projeto.Validator;
import projeto.handlers.JsonHandler;
import projeto.handlers.SceneHandler;
import projeto.handlers.StatusCode;
import projeto.models.Filme;
import projeto.requests.filmes.EditarFilmePayload;

public class EditarFilmeController {

    @FXML
    private TextArea logArea;

    @FXML
    private ListView<String> generosList;

    @FXML
    private TextField tituloField, diretorField, anoField, sinopseField;

    @FXML
    private Label status;

    private Filme filme;

    public void setFilme(Filme filme) {
        this.filme = filme;
        preencherCampos();
    }

    private void preencherCampos() {
        if (filme != null) {
            tituloField.setText(filme.getTitulo() != null ? filme.getTitulo() : "");
            diretorField.setText(filme.getDiretor() != null ? filme.getDiretor() : "");
            anoField.setText(filme.getAno() != null ? filme.getAno() : "");
            sinopseField.setText(filme.getSinopse() != null ? filme.getSinopse() : "");
            
            // Seleciona os gêneros do filme na lista
            if (filme.getGenero() != null && !filme.getGenero().isEmpty()) {
                generosList.getSelectionModel().clearSelection();
                for (String genero : filme.getGenero()) {
                    int index = generosList.getItems().indexOf(genero);
                    if (index >= 0) {
                        generosList.getSelectionModel().select(index);
                    }
                }
            }
        }
    }

    @FXML
    public void initialize() {
        LogUI.init(logArea);

        generosList.getItems().addAll(
                "Ação",
                "Comédia",
                "Drama",
                "Ficção Científica",
                "Romance");

        // permite selecionar vários de uma vez
        generosList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        // Se o filme já foi definido (caso raro), preenche os campos
        if (filme != null) {
            preencherCampos();
        }
    }

    @FXML
    private void salvar() {
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

        JsonObject filmeToSend = new JsonObject();
        filmeToSend.addProperty("id", filme.getId());
        filmeToSend.addProperty("titulo", tituloField.getText().trim());
        filmeToSend.addProperty("diretor", diretorField.getText().trim());
        filmeToSend.addProperty("ano", anoField.getText().trim());
        filmeToSend.addProperty("sinopse", sinopseField.getText().trim());
        JsonHandler.addArray(filmeToSend, "genero", generosSelecionados);

        EditarFilmePayload payload = new EditarFilmePayload(filmeToSend);
        String msg = JsonHandler.modelToString(payload);
        Session.getInstance().getOut().println(msg);
        System.out.println("Cliente -> Servidor: " + JsonHandler.prettyFormatFromString(msg));
        LogUI.log("Cliente -> Servidor: " + JsonHandler.prettyFormatFromString(msg));
        String response = null;

        try {
            response = Session.getInstance().getIn().readLine();
        } catch (Exception ex) {
            System.err.println("Erro na comunicação com o servidor: " + ex.getMessage());
        }

        JsonObject responseJson = JsonHandler.stringToJsonObject(response);
        System.out.println("Servidor -> Cliente: "
                + JsonHandler.prettyFormatFromString(JsonHandler.prettyFormatFromJson(responseJson)));
        LogUI.log("Servidor -> Cliente: "
                + JsonHandler.prettyFormatFromString(JsonHandler.prettyFormatFromJson(responseJson)));
        if (response != null && !response.isEmpty()
                && responseJson.get("status").getAsString().equals(StatusCode.OK)) {
            Session.getInstance().showAlert(AlertType.CONFIRMATION, "Salvo com sucesso", "Filme salvo com sucesso!",
                    () -> {
                        try {
                            SceneHandler.changeScene("/projeto/views/Filmes.fxml");

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
