package projeto.viewControllers;

import java.util.ArrayList;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import projeto.Session;
import projeto.handlers.JsonHandler;
import projeto.handlers.StatusCode;
import projeto.models.User;
import projeto.requests.ListUserPayload;

public class UsuariosController {

    @FXML
    private TableView<User> usuariosTable;

    @FXML
    private TableColumn<User, String> colUsuario;

    @FXML
    private TableColumn<User, Void> colAcoes;

    private final ObservableList<User> usuarios = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Configura coluna
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("usuario"));

        // Associa lista à tabela
        usuariosTable.setItems(usuarios);

        if (Session.getInstance().isAdmin()) {
            addActionButton();
        }

        carregarUsuarios();
    }

    private void addActionButton() {
        colAcoes.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnExcluir = new Button("Excluir");
            private final HBox box = new HBox(10, btnEditar, btnExcluir);

            {

                btnEditar.getStyleClass().add("button-secondary");
                btnExcluir.getStyleClass().add("button-danger");

                btnEditar.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    editarUsuario(user);
                });

                btnExcluir.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    deletarUsuario(user);
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

    private void editarUsuario(User user) {
        System.out.println("Editar -> " + user.getUsuario());
    }

    private void deletarUsuario(User user) {
        System.out.println("Deletar -> " + user.getUsuario());
    }

    private void carregarUsuarios() {
        ListUserPayload payload = new ListUserPayload(Session.getInstance().getToken());
        String msg = JsonHandler.modelToString(payload);
        JsonObject response = new JsonObject();
        System.out.println("Cliente -> Servidor: " + JsonHandler.prettyFormatFromString(msg));

        try {
            Session.getInstance().getOut().println(msg);
            response = JsonHandler.stringToJsonObject(Session.getInstance().getIn().readLine());
            System.out.println("Servidor -> Cliente: " + JsonHandler.prettyFormatFromString(response.toString()));
            if (response != null && response.get("status").getAsString().equals(StatusCode.OK)) {
                var users = response.getAsJsonArray("usuarios");

                for (JsonElement user : users) {
                    JsonObject userJson = user.getAsJsonObject();

                    String userName = userJson.get("usuario").getAsString();

                    User usuario = new User(userName);
                    usuarios.add(usuario);

                    usuariosTable.refresh();
                }
            }

        } catch (Exception ex) {
            System.err.println("Erro ao listar usuarios\n" + ex.getMessage());
        }
    }
}
