package projeto.viewControllers;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import projeto.LogUI;
import projeto.Session;
import projeto.handlers.JsonHandler;
import projeto.handlers.SceneHandler;
import projeto.handlers.StatusCode;
import projeto.models.Filme;
import projeto.models.Review;
import projeto.requests.filmes.GetFilmePayload;

public class VerFilmeController {

    @FXML
    private TextArea logArea;

    @FXML
    private Label tituloLabel, diretorLabel, anoLabel, sinopseLabel, notaLabel, avaliacoesLabel, statusLabel;

    @FXML
    private ListView<String> generosList;

    @FXML
    private ListView<String> reviewsList;

    private String filmeId;

    public void setFilmeId(String id) {
        this.filmeId = id;
        carregarFilme();
    }

    @FXML
    public void initialize() {
        LogUI.init(logArea);
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
            ObservableList<String> reviewsDisplay = FXCollections.observableArrayList();
            if (reviews != null && !reviews.isEmpty()) {
                for (Review review : reviews) {
                    StringBuilder reviewText = new StringBuilder();
                    reviewText.append("★ ").append(review.getNota() != null ? review.getNota() : "N/A");
                    reviewText.append(" | ").append(review.getTitulo() != null ? review.getTitulo() : "Sem título");
                    reviewText.append(" | Por: ").append(review.getUsuario() != null ? review.getUsuario() : "Anônimo");
                    if (review.getData() != null) {
                        reviewText.append(" | ").append(review.getData());
                    }
                    reviewText.append("\n").append(review.getDescricao() != null ? review.getDescricao() : "");
                    reviewsDisplay.add(reviewText.toString());
                }
            } else {
                reviewsDisplay.add("Nenhuma avaliação disponível.");
            }
            reviewsList.setItems(reviewsDisplay);
        }
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

