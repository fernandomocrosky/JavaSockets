package projeto.viewControllers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javafx.beans.property.SimpleStringProperty;
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
import projeto.models.Review;
import projeto.requests.reviews.EditarReviewPayload;
import projeto.requests.reviews.ExcluirReviewPayload;
import projeto.requests.reviews.ListReviewsUsuarioPayload;

public class ReviewsController {

    @FXML
    private TextArea logArea;

    @FXML
    private TableView<Review> reviewsTable;

    @FXML
    private TableColumn<Review, String> colId;

    @FXML
    private TableColumn<Review, String> colIdFilme;

    @FXML
    private TableColumn<Review, String> colTitulo;

    @FXML
    private TableColumn<Review, String> colDescricao;

    @FXML
    private TableColumn<Review, String> colNota;

    @FXML
    private TableColumn<Review, String> colData;

    @FXML
    private TableColumn<Review, String> colEditado;

    @FXML
    private TableColumn<Review, Void> colAcoes;

    private final ObservableList<Review> reviews = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        LogUI.init(logArea);

        // Configura colunas
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colIdFilme.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getFilme()));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colNota.setCellValueFactory(new PropertyValueFactory<>("nota"));
        colData.setCellValueFactory(new PropertyValueFactory<>("data"));
        colEditado.setCellValueFactory(new PropertyValueFactory<>("editado"));

        // Associa lista à tabela
        reviewsTable.setItems(reviews);

        addActionButton();

        carregarReviews();
    }

    private void addActionButton() {
        colAcoes.setCellFactory(col -> new TableCell<>() {
            private final Button btnVerFilme = new Button("Ver Filme");
            private final Button btnEditar = new Button("Editar");
            private final Button btnExcluir = new Button("Excluir");
            private final HBox box = new HBox(10, btnVerFilme, btnEditar, btnExcluir);
            {
                box.setAlignment(javafx.geometry.Pos.CENTER);

                btnVerFilme.getStyleClass().add("button-secondary");
                btnEditar.getStyleClass().add("button-secondary");
                btnExcluir.getStyleClass().add("button-danger");

                btnVerFilme.setOnAction(e -> {
                    Review review = getTableView().getItems().get(getIndex());
                    verFilme(review);
                });

                btnEditar.setOnAction(e -> {
                    Review review = getTableView().getItems().get(getIndex());
                    editarReview(review);
                });

                btnExcluir.setOnAction(e -> {
                    Review review = getTableView().getItems().get(getIndex());
                    excluirReview(review);
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

    private void verFilme(Review review) {
        SceneHandler.changeSceneWithData("/projeto/views/VerFilme.fxml",
                (VerFilmeController controller) -> controller.setFilmeId(review.getFilme()));
    }

    private void editarReview(Review review) {
        SceneHandler.changeSceneWithData("/projeto/views/EditarReview.fxml",
                (EditarReviewController controller) -> controller.setReview(review));
    }

    private void excluirReview(Review review) {
        Session.getInstance().showAlert(AlertType.CONFIRMATION, "Excluir Review",
                "Tem certeza que deseja excluir esta review?", () -> {
                    ExcluirReviewPayload payload = new ExcluirReviewPayload(review.getId());
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
                            Session.getInstance().showAlert(AlertType.INFORMATION, "Excluído com sucesso",
                                    "Review excluída com sucesso",
                                    () -> {
                                        reviews.clear();
                                        carregarReviews();
                                    });
                        }
                    } catch (Exception ex) {
                        System.err.println("Erro ao excluir review\n" + ex.getMessage());
                    }
                });
    }

    private void carregarReviews() {
        ListReviewsUsuarioPayload payload = new ListReviewsUsuarioPayload();
        String msg = JsonHandler.modelToString(payload);
        JsonObject response = new JsonObject();
        System.out.println("Cliente -> Servidor: " + JsonHandler.prettyFormatFromString(msg));
        LogUI.log("Cliente -> Servidor: " + JsonHandler.prettyFormatFromString(msg));

        try {
            Session.getInstance().getOut().println(msg);
            response = JsonHandler.stringToJsonObject(Session.getInstance().getIn().readLine());
            System.out.println("Servidor -> Cliente: " + JsonHandler.prettyFormatFromString(response.toString()));
            LogUI.log("Servidor -> Cliente: " + JsonHandler.prettyFormatFromString(response.toString()));
            
            if (response != null && response.get("status").getAsString().equals(StatusCode.OK)) {
                reviews.clear();
                var reviewsJson = response.getAsJsonArray("reviews");

                for (JsonElement reviewElement : reviewsJson) {
                    Review review = JsonHandler.jsonToModel(reviewElement.getAsJsonObject(), Review.class);
                    reviews.add(review);
                }
                
                reviewsTable.refresh();
            }
        } catch (Exception ex) {
            System.err.println("Erro ao listar reviews\n" + ex.getMessage());
        }
    }
}

