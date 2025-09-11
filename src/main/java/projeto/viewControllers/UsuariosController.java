package projeto.viewControllers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
import projeto.models.User;
import projeto.requests.user.DeleteUserPayload;
import projeto.requests.user.ListUserPayload;

public class UsuariosController {

    @FXML
    private TextArea logArea;

    @FXML
    private TableView<User> usuariosTable;

    @FXML
    private TableColumn<User, String> colUsuario;

    @FXML
    private TableColumn<User, String> colId;

    @FXML
    private TableColumn<User, Void> colAcoes;

    private final ObservableList<User> usuarios = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        LogUI.init(logArea);

        // Configura coluna
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("usuario"));
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

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
                box.setAlignment(javafx.geometry.Pos.CENTER);

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
        SceneHandler.changeSceneWithData("/projeto/views/EditarUsuario.fxml",
                (EditarUsuarioController controller) -> controller.setUsuario(user));
    }

    private void deletarUsuario(User user) {
        Session.getInstance().showAlert(AlertType.CONFIRMATION, "Excluir usuário",
                "Tem certeza que deseja excluir o usuário " + user.getUsuario() + "?",
                () -> {
                    DeleteUserPayload payload = new DeleteUserPayload(user.getId());
                    String msg = JsonHandler.modelToString(payload);
                    System.out.println("Cliente -> Servidor: " + JsonHandler.prettyFormatFromString(msg));
                    LogUI.log("Cliente -> Servidor: " + JsonHandler.prettyFormatFromString(msg));
                    try {
                        Session.getInstance().getOut().println(msg);
                        JsonObject response = JsonHandler.stringToJsonObject(Session.getInstance().getIn().readLine());
                        System.out.println(
                                "Servidor -> Cliente: " + JsonHandler.prettyFormatFromString(response.toString()));
                        LogUI.log("Servidor -> Cliente: " + JsonHandler.prettyFormatFromString(response.toString()));
                        if (response != null && response.get("status").getAsString().equals(StatusCode.OK)) {
                            SceneHandler.changeScene("/projeto/views/Usuarios.fxml");
                        }
                    } catch (Exception ex) {
                        System.err.println("Erro ao excluir usuario\n" + ex.getMessage());
                    }
                });
    }

    private void carregarUsuarios() {
        ListUserPayload payload = new ListUserPayload();
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
                var users = response.getAsJsonArray("usuarios");

                for (JsonElement user : users) {
                    JsonObject userJson = user.getAsJsonObject();

                    String id = userJson.get("id").getAsString();
                    String userName = userJson.get("usuario").getAsString();

                    User usuario = new User(userName);
                    usuario.setId(id);

                    usuarios.add(usuario);

                    usuariosTable.refresh();
                }
            }

        } catch (Exception ex) {
            System.err.println("Erro ao listar usuarios\n" + ex.getMessage());
        }
    }
}
