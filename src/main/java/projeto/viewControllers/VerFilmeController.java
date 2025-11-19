package projeto.viewControllers;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import projeto.LogUI;
import projeto.Session;
import projeto.handlers.JsonHandler;
import projeto.handlers.JwtHandle;
import projeto.handlers.SceneHandler;
import projeto.handlers.StatusCode;
import projeto.models.Filme;
import projeto.models.Review;
import projeto.requests.filmes.GetFilmePayload;
import projeto.requests.reviews.ExcluirReviewPayload;

public class VerFilmeController {

    @FXML
    private TextArea logArea;

    @FXML
    private Label tituloLabel, diretorLabel, anoLabel, sinopseLabel, notaLabel, avaliacoesLabel, statusLabel;

    @FXML
    private ListView<String> generosList;

    @FXML
    private TableView<Review> reviewsTable;

    @FXML
    private TableColumn<Review, String> colNota;

    @FXML
    private TableColumn<Review, String> colTitulo;

    @FXML
    private TableColumn<Review, String> colUsuario;

    @FXML
    private TableColumn<Review, String> colData;

    @FXML
    private TableColumn<Review, String> colEditado;

    @FXML
    private TableColumn<Review, Void> colAcoes;

    @FXML
    private Button btnCriarReview;

    private String filmeId;
    private String userId;
    private String role;
    private final ObservableList<Review> reviews = FXCollections.observableArrayList();

    public void setFilmeId(String id) {
        this.filmeId = id;
        carregarFilme();
    }

    @FXML
    public void initialize() {
        LogUI.init(logArea);
        
        // Obter informações do usuário logado
        String token = Session.getInstance().getToken();
        if (token != null) {
            userId = JwtHandle.getClaim(token, "id", String.class);
            role = JwtHandle.getClaim(token, "funcao", String.class);
        }
        
        // Configurar colunas da tabela
        colNota.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("nota"));
        colTitulo.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("titulo"));
        colUsuario.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getUsuario() != null ? cellData.getValue().getUsuario() : "Anônimo"));
        colData.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("data"));
        colEditado.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("editado"));
        
        // Configurar coluna de ações
        addActionButton();
        
        // Associar lista à tabela
        reviewsTable.setItems(reviews);
        
        // Mostrar botão de criar review apenas para admin
        if (btnCriarReview != null) {
            btnCriarReview.setVisible("admin".equals(role));
        }
    }
    
    private void addActionButton() {
        colAcoes.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnExcluir = new Button("Excluir");
            private final HBox box = new HBox(10, btnEditar, btnExcluir);
            
            {
                box.setAlignment(javafx.geometry.Pos.CENTER);
                btnEditar.getStyleClass().add("button-secondary");
                btnExcluir.getStyleClass().add("button-danger");
                
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
                    Review review = getTableView().getItems().get(getIndex());
                    // Mostrar botões apenas se for admin ou se a review pertencer ao usuário
                    boolean podeEditar = "admin".equals(role) || 
                                       (userId != null && review.getIdUsuario() != null && review.getIdUsuario().equals(userId));
                    btnEditar.setVisible(podeEditar);
                    btnExcluir.setVisible(podeEditar);
                    setGraphic(box);
                }
            }
        });
    }

    private void carregarFilme() {
        if (filmeId == null || filmeId.isEmpty()) {
            statusLabel.setText("ID do filme não fornecido");
            return;
        }

        GetFilmePayload payload = new GetFilmePayload(filmeId);
        String msg = JsonHandler.modelToString(payload);
        String response = null;

        try {
            Session.getInstance().getOut().println(msg);
            System.out.println("Cliente -> Servidor: " + JsonHandler.prettyFormatFromString(msg));
            LogUI.log("Cliente -> Servidor: " + JsonHandler.prettyFormatFromString(msg));
            response = Session.getInstance().getIn().readLine();

            JsonObject responseJson = JsonHandler.stringToJsonObject(response);
            System.out.println("Servidor -> Cliente: " + JsonHandler.prettyFormatFromString(JsonHandler.prettyFormatFromJson(responseJson)));
            LogUI.log("Servidor -> Cliente: " + JsonHandler.prettyFormatFromString(JsonHandler.prettyFormatFromJson(responseJson)));

            if (response != null && !response.isEmpty()
                    && responseJson.get("status").getAsString().equals(StatusCode.OK)) {
                JsonObject filmeJson = responseJson.getAsJsonObject("filme");
                Filme filme = JsonHandler.jsonToModel(filmeJson, Filme.class);
                
                // Buscar reviews
                JsonArray reviewsArray = responseJson.getAsJsonArray("reviews");
                List<Review> reviews = null;
                if (reviewsArray != null) {
                    reviews = new java.util.ArrayList<>();
                    for (JsonElement reviewElement : reviewsArray) {
                        Review review = JsonHandler.jsonToModel(reviewElement.getAsJsonObject(), Review.class);
                        reviews.add(review);
                    }
                }
                
                preencherCampos(filme, reviews);
            } else {
                statusLabel.setText(StatusCode.getMessage(responseJson.get("status").getAsString()));
            }
        } catch (Exception ex) {
            System.err.println("Erro ao carregar filme: " + ex.getMessage());
            statusLabel.setText("Erro ao carregar filme: " + ex.getMessage());
        }
    }

    private void preencherCampos(Filme filme, List<Review> reviews) {
        if (filme != null) {
            tituloLabel.setText(filme.getTitulo() != null ? filme.getTitulo() : "N/A");
            diretorLabel.setText(filme.getDiretor() != null ? filme.getDiretor() : "N/A");
            anoLabel.setText(filme.getAno() != null ? filme.getAno() : "N/A");
            sinopseLabel.setText(filme.getSinopse() != null ? filme.getSinopse() : "Sinopse não disponível.");
            
            // Preencher gêneros
            if (filme.getGenero() != null && !filme.getGenero().isEmpty()) {
                ObservableList<String> generos = FXCollections.observableArrayList(filme.getGenero());
                generosList.setItems(generos);
            } else {
                ObservableList<String> generos = FXCollections.observableArrayList();
                generos.add("Nenhum gênero cadastrado");
                generosList.setItems(generos);
            }
            
            notaLabel.setText(filme.getNota() != null ? filme.getNota() : "N/A");
            avaliacoesLabel.setText(filme.getQtdAvaliacoes() != null ? filme.getQtdAvaliacoes() : "0");
            
            // Preencher reviews
            this.reviews.clear();
            if (reviews != null && !reviews.isEmpty()) {
                this.reviews.addAll(reviews);
            }
            reviewsTable.refresh();
        }
    }
    
    @FXML
    private void criarReview() {
        SceneHandler.changeSceneWithData("/projeto/views/CadastroReview.fxml",
                (CadastroReviewController controller) -> {
                    Filme filme = new Filme();
                    filme.id = filmeId;
                    controller.setFilme(filme);
                });
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
                                        carregarFilme(); // Recarregar filme para atualizar reviews
                                    });
                        } else {
                            String mensagem = responseJson.has("mensagem") 
                                    ? responseJson.get("mensagem").getAsString() 
                                    : "Erro ao excluir review";
                            Session.getInstance().showAlert(AlertType.ERROR, "Erro", mensagem, null);
                        }
                    } catch (Exception ex) {
                        System.err.println("Erro ao excluir review\n" + ex.getMessage());
                        Session.getInstance().showAlert(AlertType.ERROR, "Erro", 
                                "Erro ao excluir review: " + ex.getMessage(), null);
                    }
                });
    }

    @FXML
    private void voltar() {
        try {
            SceneHandler.changeScene("/projeto/views/Filmes.fxml");
        } catch (Exception e) {
            System.out.println("Erro ao trocar de tela: " + e.getMessage());
        }
    }
}

