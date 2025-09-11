package projeto.viewControllers;

import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import projeto.LogUI;
import projeto.Session;
import projeto.handlers.JsonHandler;
import projeto.handlers.SceneHandler;
import projeto.handlers.StatusCode;
import projeto.models.Filme;
import projeto.requests.filmes.DeleteFilmePayload;
import projeto.requests.filmes.ListFilmePayload;

public class FilmesController {
    @FXML
    private TextArea logArea;

    @FXML
    private TableView<Filme> filmesTable;

    @FXML
    private TableColumn<Filme, String> colId;

    @FXML
    private TableColumn<Filme, String> colTitulo;

    @FXML
    private TableColumn<Filme, String> colSinopse;

    @FXML
    private TableColumn<Filme, String> colDiretor;

    @FXML
    private TableColumn<Filme, String> colAno;

    @FXML
    private TableColumn<Filme, List<String>> colGeneros;

    @FXML
    private TableColumn<Filme, String> colNota;

    @FXML
    private TableColumn<Filme, String> colAvaliacoes;

    @FXML
    private TableColumn<Filme, Void> colAcoes;

    private final ObservableList<Filme> filmes = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        LogUI.init(logArea);

        // Configura coluna
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colSinopse.setCellValueFactory(new PropertyValueFactory<>("sinopse"));
        colDiretor.setCellValueFactory(new PropertyValueFactory<>("diretor"));
        colAno.setCellValueFactory(new PropertyValueFactory<>("ano"));
        colGeneros.setCellValueFactory(new PropertyValueFactory<>("generos"));

        // colGeneros.setCellValueFactory(
        // cellData -> new SimpleStringProperty(String.join(", ",
        // cellData.getValue().generos)));
        colNota.setCellValueFactory(new PropertyValueFactory<>("nota"));
        colAvaliacoes.setCellValueFactory(new PropertyValueFactory<>("qtdAvaliacoes"));

        // Associa lista à tabela
        filmesTable.setItems(filmes);

        if (Session.getInstance().isAdmin()) {
            addActionAdminButton();
        } else {
            addActionUserButton();
        }

        carregarFilmes();
    }

    private void addActionAdminButton() {
        colAcoes.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnExcluir = new Button("Excluir");
            private final HBox box = new HBox(10, btnEditar, btnExcluir);

            {
                box.setAlignment(javafx.geometry.Pos.CENTER);

                btnEditar.getStyleClass().add("button-secondary");
                btnExcluir.getStyleClass().add("button-danger");

                btnEditar.setOnAction(e -> {
                    Filme filme = getTableView().getItems().get(getIndex());
                    editarFilme(filme);
                });

                btnExcluir.setOnAction(e -> {
                    Filme filme = getTableView().getItems().get(getIndex());
                    deletarFilme(filme);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(box);
                }
            }
        });
    }

    private void addActionUserButton() {
        colAcoes.setCellFactory(col -> new TableCell<>() {
            private final Button btnReview = new Button("Criar Review");
            private final HBox box = new HBox(10, btnReview);

            {
                box.setAlignment(javafx.geometry.Pos.CENTER);

                btnReview.getStyleClass().add("button-secondary");

                btnReview.setOnAction(e -> {
                    Filme filme = getTableView().getItems().get(getIndex());
                    avaliarFilme(filme);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(box);
                }
            }
        });
    }

    private void editarFilme(Filme filme) {
        SceneHandler.changeSceneWithData("/projeto/views/EditarFilme.fxml",
                (EditarFilmeController controller) -> controller.setFilme(filme));
    }

    private void deletarFilme(Filme filme) {
        Session.getInstance().showAlert(AlertType.CONFIRMATION, "Excluir Filme",
                "Tem certeza que deseja excluir o filme " + filme.getTitulo() + "?", () -> {
                    DeleteFilmePayload payload = new DeleteFilmePayload(filme.getId());
                    String msg = JsonHandler.modelToString(payload);
                    String response = null;

                    try {
                        Session.getInstance().getOut().println(msg);
                        System.out.println("Cliente -> Servidor: " + JsonHandler.prettyFormatFromString(msg));
                        LogUI.log("Cliente -> Servidor: " + JsonHandler.prettyFormatFromString(msg));
                        response = Session.getInstance().getIn().readLine();

                        JsonObject responseJson = JsonHandler.stringToJsonObject(response);
                        System.out.println(
                                "Servidor -> Cliente: " + JsonHandler.prettyFormatFromString(responseJson.toString()));
                        LogUI.log(
                                "Servidor -> Cliente: " + JsonHandler.prettyFormatFromString(responseJson.toString()));
                        if (responseJson != null && responseJson.get("status").getAsString().equals(StatusCode.OK)) {
                            Session.getInstance().showAlert(AlertType.CONFIRMATION, "Excluido com sucesso",
                                    "Filme excluido com sucesso",
                                    () -> SceneHandler.changeScene("/projeto/views/Filmes.fxml"));
                        }
                    } catch (Exception ex) {
                        System.err.println("Erro ao excluir filme\n" + ex.getMessage());
                    }
                });
    }

    private void avaliarFilme(Filme filme) {
        SceneHandler.changeSceneWithData("/projeto/views/CadastroReview.fxml",
                (CadastroReviewController controller) -> controller.setFilme(filme));
    }

    private void carregarFilmes() {
        ListFilmePayload payload = new ListFilmePayload();
        String msg = JsonHandler.modelToString(payload);
        JsonObject response = new JsonObject();
        System.out.println("Cliente -> Servidor: " + JsonHandler.prettyFormatFromString(msg));
        LogUI.log("Cliente -> Servidor: " + JsonHandler.prettyFormatFromString(msg));
        try {
            Session.getInstance().getOut().println(msg);
            response = JsonHandler.stringToJsonObject(Session.getInstance().getIn().readLine());
            System.out.println("Servidor -> Cliente: " + JsonHandler.prettyFormatFromJson(response));
            LogUI.log("Servidor -> Cliente: " + JsonHandler.prettyFormatFromJson(response));

            if (response != null && response.get("status").getAsString().equals(StatusCode.OK)) {
                var filmesJson = response.getAsJsonArray("filmes");

                for (JsonElement filmeJson : filmesJson) {
                    Filme filme = JsonHandler.jsonToModel(filmeJson.getAsJsonObject(), Filme.class);
                    filmes.add(filme);
                }
            }
        } catch (Exception ex) {
            System.err.println("Erro ao listar filmes\n" + ex.getMessage());
        }
    }
}
