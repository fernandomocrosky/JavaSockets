package projeto.viewControllers;

import com.google.gson.JsonObject;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert.AlertType;
import projeto.LogUI;
import projeto.Session;
import projeto.handlers.JsonHandler;
import projeto.handlers.SceneHandler;
import projeto.handlers.StatusCode;
import projeto.requests.user.DeleteUserPayload;

public class HomeController {

    @FXML
    private TextArea logArea;

    @FXML
    private Label welcomeLabel;

    @FXML
    private Button createFilme;

    @FXML
    private Button editUser;

    @FXML
    private Button deleteUser;

    @FXML
    private Label infoLabel;

    @FXML
    public void initialize() {
        LogUI.init(logArea);

        // pega o usuário atual da Session
        String token = Session.getInstance().getToken();
        if (token != null) {
            String usuario = Session.getInstance().getUser().getUsuario();

            welcomeLabel.setText("Olá, " + usuario + "!");
        }

        if (!Session.getInstance().isAdmin()) {
            createFilme.setVisible(false);
        }
    }

    @FXML
    private void goCreateFilme() {
        try {
            SceneHandler.changeScene("/projeto/views/CadastroFilme.fxml");
        } catch (Exception e) {
            System.out.println("Erro ao trocar de tela: " + e.getMessage());
        }
    }

    @FXML
    private void goEditUser() {
        try {
            SceneHandler.changeSceneWithData("/projeto/views/EditarMeuUsuario.fxml",
                    (EditarMeuUsuarioController controller) -> controller.setUsuario(Session.getInstance().getUser()));
        } catch (Exception e) {
            System.out.println("Erro ao trocar de tela: " + e.getMessage());
        }
    }

    @FXML
    private void goDeleteUser() {
        try {
            Session.getInstance().showAlert(AlertType.CONFIRMATION, "Excluir usuário",
                    "Tem certeza que deseja excluir o seu usuário?", () -> {
                        DeleteUserPayload payload = new DeleteUserPayload();
                        String msg = JsonHandler.modelToString(payload);
                        Session.getInstance().getOut().println(msg);
                        System.out.println("Cliente -> Servidor: " + JsonHandler.prettyFormatFromString(msg));
                        LogUI.log("Cliente -> Servidor: " + JsonHandler.prettyFormatFromString(msg));
                        try {
                            JsonObject response = JsonHandler
                                    .stringToJsonObject(Session.getInstance().getIn().readLine());
                            System.out.println(
                                    "Servidor -> Cliente: " + JsonHandler.prettyFormatFromString(response.toString()));
                            LogUI.log(
                                    "Servidor -> Cliente: " + JsonHandler.prettyFormatFromString(response.toString()));
                            if (response != null && response.get("status").getAsString().equals(StatusCode.OK)) {
                                Session.getInstance().showAlert(AlertType.CONFIRMATION, "Excluido com sucesso",
                                        "Usuário excluido com sucesso",
                                        () -> {
                                            try {
                                                // Tenta fazer logout antes de fechar a conexão
                                                Session.getInstance().desconectar();
                                            } catch (Exception e) {
                                                // Ignora erros de logout se a conexão já estiver fechada
                                                System.out.println("Erro ao fazer logout: " + e.getMessage());
                                            } finally {
                                                // Fecha explicitamente o socket para que o servidor detecte a desconexão
                                                Session.getInstance().clear();
                                                SceneHandler.changeScene("/projeto/views/CONEXAO.fxml");
                                            }
                                        });
                            }
                        } catch (Exception e) {
                            System.out.println("Erro ao excluir seu usuário: " + e.getMessage());
                        }
                    });
        } catch (Exception e) {
            System.out.println("Erro ao excluir seu usuário: " + e.getMessage());
        }
    }

    // @FXML
    // private void logout() {
    // JsonObject logoutResponse = Session.getInstance().desconectar();
    // System.out
    // .println("\nServidor -> Cliente: " +
    // JsonHandler.prettyFormatFromString(logoutResponse.toString()));

    // if (logoutResponse.get("status").getAsString().equals(StatusCode.OK)) {
    // try {
    // FXMLLoader loader = new
    // FXMLLoader(getClass().getResource("/projeto/views/CONEXAO.fxml"));
    // Scene conexaoScene = new Scene(loader.load(), 420, 420);
    // Stage stage = (Stage) welcomeLabel.getScene().getWindow();
    // stage.setScene(conexaoScene);
    // } catch (Exception e) {
    // System.out.println("Erro ao trocar de tela: " + e.getMessage());
    // }
    // } else {
    // showError(logoutResponse.get("message").getAsString());
    // }
    // }
}
